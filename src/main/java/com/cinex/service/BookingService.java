package com.cinex.service;

import com.cinex.domain.*;
import com.cinex.dto.CheckoutForm;
import com.cinex.exception.BadRequestException;
import com.cinex.exception.NotFoundException;
import com.cinex.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final TicketRepository ticketRepository;
    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final PromotionRepository promotionRepository;
    private final UserRepository userRepository;
    private final CodeGenerator codeGenerator;

    /** Số phút giữ ghế trước khi đơn pending tự động được giải phóng. */
    @Value("${app.hold-expire-minutes:15}")
    private int holdExpireMinutes;

    public int holdExpireMinutes() {
        return holdExpireMinutes;
    }

    /**
     * Giữ ghế: tạo booking ở trạng thái {@code pending} và ghi các ghế được chọn.
     * Việc chống bán trùng ghế do ràng buộc UNIQUE (showtime_id, seat_id) đảm bảo.
     */
    @Transactional
    public Booking holdSeats(User user, CheckoutForm form) {
        Showtime showtime = showtimeRepository.findById(form.getShowtimeId())
                .orElseThrow(() -> NotFoundException.of("suất chiếu", form.getShowtimeId()));

        if (showtime.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Suất chiếu đã bắt đầu, không thể đặt vé");
        }

        List<Seat> seats = seatRepository.findAllById(form.getSeatIds());
        if (seats.size() != form.getSeatIds().size()) {
            throw new BadRequestException("Có ghế không tồn tại trong phòng này");
        }
        for (Seat seat : seats) {
            if (!seat.getScreen().getId().equals(showtime.getScreen().getId())) {
                throw new BadRequestException("Ghế " + seat.getLabel() + " không thuộc phòng của suất chiếu");
            }
        }

        // Ghế đã có người giữ hoặc đã bán trong suất chiếu này
        List<Integer> taken = bookingSeatRepository.findTakenSeatIds(showtime.getId());
        String takenLabels = seats.stream()
                .filter(seat -> taken.contains(seat.getId()))
                .map(Seat::getLabel)
                .collect(Collectors.joining(", "));
        if (!takenLabels.isBlank()) {
            throw new BadRequestException("Ghế " + takenLabels + " đã được giữ hoặc đã bán, vui lòng chọn lại");
        }

        Booking booking = new Booking();
        booking.setCode(generateCode("BK"));
        booking.setUser(user);
        booking.setShowtime(showtime);
        booking.setStatus(Booking.STATUS_PENDING);
        booking.setPaymentMethod(form.getPaymentMethod() == null ? "cash" : form.getPaymentMethod());
        booking.setTotalAmount(0);
        booking.setDiscountAmount(0);
        booking.setFinalAmount(0);
        booking = bookingRepository.save(booking);

        int total = 0;
        for (Seat seat : seats) {
            int price = Seat.TYPE_VIP.equalsIgnoreCase(seat.getSeatType())
                    ? showtime.getVipPrice()
                    : showtime.getBasePrice();
            total += price;

            BookingSeat bookingSeat = new BookingSeat();
            bookingSeat.setBooking(booking);
            bookingSeat.setShowtime(showtime);
            bookingSeat.setSeat(seat);
            bookingSeat.setSeatLabel(seat.getLabel());
            bookingSeat.setPrice(price);
            bookingSeat.setStatus(BookingSeat.STATUS_RESERVED);
            try {
                // saveAndFlush để ràng buộc UNIQUE (showtime_id, seat_id) được kiểm tra ngay,
                // bắt được trường hợp hai người cùng bấm chọn một ghế.
                bookingSeatRepository.saveAndFlush(bookingSeat);
            } catch (DataIntegrityViolationException ex) {
                throw new BadRequestException("Ghế " + seat.getLabel()
                        + " vừa được người khác chọn, vui lòng chọn ghế khác");
            }
        }

        booking.setTotalAmount(total);
        booking.setFinalAmount(total);
        return bookingRepository.save(booking);
    }

    /** Xác nhận thanh toán: chuyển booking sang confirmed, sinh vé và hoá đơn. */
    @Transactional
    public Booking confirmBooking(User user, String bookingCode, String promotionCode,
                                  String paymentMethod) {
        Booking booking = requireOwnedBooking(user, bookingCode);
        if (!Booking.STATUS_PENDING.equals(booking.getStatus())) {
            throw new BadRequestException("Đơn đặt vé đã được xử lý");
        }

        List<BookingSeat> bookingSeats = requireSeats(booking);
        int subtotal = bookingSeats.stream().mapToInt(BookingSeat::getPrice).sum();
        int discount = applyPromotion(booking, promotionCode, subtotal);

        booking.setTotalAmount(subtotal);
        booking.setDiscountAmount(discount);
        booking.setFinalAmount(subtotal - discount);
        booking.setPaymentMethod(paymentMethod == null ? "cash" : paymentMethod);
        booking.setStatus(Booking.STATUS_CONFIRMED);
        bookingRepository.save(booking);

        bookingSeats.forEach(bookingSeat ->
                bookingSeat.setStatus(BookingSeat.STATUS_PAID));
        bookingSeatRepository.saveAll(bookingSeats);

        issueTickets(booking, bookingSeats);
        createBill(booking, subtotal, discount);
        settlePayment(booking, booking.getPaymentMethod(), null, null);
        return booking;
    }

    /**
     * Chuẩn bị thanh toán online: chốt tiền và áp mã khuyến mãi,
     * đơn vẫn giữ trạng thái {@code pending} cho tới khi PayOS báo đã thu tiền.
     */
    @Transactional
    public Booking prepareOnlinePayment(User user, String bookingCode, String promotionCode) {
        Booking booking = requireOwnedBooking(user, bookingCode);
        if (!Booking.STATUS_PENDING.equals(booking.getStatus())) {
            throw new BadRequestException("Đơn đặt vé không còn ở trạng thái chờ thanh toán");
        }

        List<BookingSeat> bookingSeats = requireSeats(booking);
        int subtotal = bookingSeats.stream().mapToInt(BookingSeat::getPrice).sum();
        int discount = applyPromotion(booking, promotionCode, subtotal);

        booking.setTotalAmount(subtotal);
        booking.setDiscountAmount(discount);
        booking.setFinalAmount(subtotal - discount);
        booking.setPaymentMethod("payos");
        return bookingRepository.save(booking);
    }

    /**
     * Đánh dấu đơn đã thanh toán qua cổng online (PayOS).
     * Gọi lại nhiều lần vẫn an toàn: đơn đã xác nhận thì trả về luôn.
     */
    @Transactional
    public Booking markAsPaid(String bookingCode, String providerOrderId, String providerRef) {
        Booking booking = bookingRepository.findByCode(bookingCode)
                .orElseThrow(() -> NotFoundException.of("đơn đặt vé", bookingCode));
        if (Booking.STATUS_CONFIRMED.equals(booking.getStatus())) {
            return booking;
        }
        if (!Booking.STATUS_PENDING.equals(booking.getStatus())) {
            throw new BadRequestException("Đơn đặt vé đã được xử lý");
        }

        List<BookingSeat> bookingSeats = requireSeats(booking);
        booking.setStatus(Booking.STATUS_CONFIRMED);
        bookingRepository.save(booking);

        bookingSeats.forEach(bookingSeat ->
                bookingSeat.setStatus(BookingSeat.STATUS_PAID));
        bookingSeatRepository.saveAll(bookingSeats);

        issueTickets(booking, bookingSeats);
        createBill(booking, booking.getTotalAmount(), booking.getDiscountAmount());
        settlePayment(booking, "payos", providerOrderId, providerRef);
        return booking;
    }

    /** Tìm đơn và kiểm tra người dùng có quyền thao tác hay không. */
    private Booking requireOwnedBooking(User user, String bookingCode) {
        Booking booking = bookingRepository.findByCode(bookingCode)
                .orElseThrow(() -> NotFoundException.of("đơn đặt vé", bookingCode));
        if (!booking.getUser().getId().equals(user.getId()) && !user.isAdmin()) {
            throw new BadRequestException("Không có quyền thao tác đơn đặt vé này");
        }
        return booking;
    }

    private List<BookingSeat> requireSeats(Booking booking) {
        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingIdOrderByIdAsc(
                booking.getId());
        if (bookingSeats.isEmpty()) {
            throw new BadRequestException("Đơn đặt vé không có ghế nào");
        }
        return bookingSeats;
    }

    /**
     * Áp mã khuyến mãi và trả về số tiền được giảm.
     * Mã không hợp lệ hoặc chưa tới hạn mức sẽ ném lỗi, không ghi mã vào đơn.
     */
    private int applyPromotion(Booking booking, String promotionCode, int subtotal) {
        if (promotionCode == null || promotionCode.isBlank()) {
            return 0;
        }
        Promotion promotion = promotionRepository.findByCodeIgnoreCase(promotionCode.trim())
                .orElseThrow(() -> new BadRequestException("Mã khuyến mãi không tồn tại"));
        if (!promotion.isUsableOn(LocalDateTime.now().toLocalDate())) {
            throw new BadRequestException("Mã khuyến mãi không còn hiệu lực");
        }
        if (subtotal < promotion.getMinAmount()) {
            throw new BadRequestException("Mã " + promotion.getCode()
                    + " chỉ áp dụng cho đơn từ " + formatMoney(promotion.getMinAmount()));
        }
        int discount = promotion.calculateDiscount(subtotal);
        if (discount <= 0) {
            throw new BadRequestException("Mã khuyến mãi không áp dụng được cho đơn này");
        }
        // Tăng lượt dùng bằng câu lệnh cập nhật trực tiếp để tránh mất cập nhật khi có nhiều request đồng thời.
        promotionRepository.incrementUsedCount(promotion.getId());
        booking.setPromotionCode(promotion.getCode());
        return discount;
    }

    private void issueTickets(Booking booking, List<BookingSeat> bookingSeats) {
        for (BookingSeat bookingSeat : bookingSeats) {
            Ticket ticket = new Ticket();
            ticket.setTicketCode(generateCode("TK"));
            ticket.setBooking(booking);
            ticket.setUser(booking.getUser());
            ticket.setShowtime(booking.getShowtime());
            ticket.setSeatLabel(bookingSeat.getSeatLabel());
            ticket.setQrCode(booking.getCode() + "-" + bookingSeat.getSeatLabel());
            ticket.setStatus(Ticket.STATUS_ISSUED);
            ticketRepository.save(ticket);
        }
    }

    private void createBill(Booking booking, int subtotal, int discount) {
        Bill bill = new Bill();
        bill.setCode(generateCode("BL"));
        bill.setBooking(booking);
        bill.setUser(booking.getUser());
        bill.setSubtotal(subtotal);
        bill.setDiscount(discount);
        bill.setTotal(subtotal - discount);
        billRepository.save(bill);
    }

    /**
     * Ghi nhận thanh toán. Với thanh toán online, bản ghi pending đã tạo sẵn
     * được cập nhật lại thay vì tạo mới.
     */
    private void settlePayment(Booking booking, String provider, String providerOrderId,
                               String providerRef) {
        Payment payment = paymentRepository
                .findByBookingIdAndStatus(booking.getId(), Payment.STATUS_PENDING)
                .orElseGet(() -> {
                    Payment created = new Payment();
                    created.setBooking(booking);
                    created.setUser(booking.getUser());
                    return created;
                });
        payment.setProvider(provider);
        payment.setAmount(booking.getFinalAmount());
        payment.setStatus(Payment.STATUS_PAID);
        payment.setPaidAt(LocalDateTime.now());
        if (payment.getCode() == null) {
            payment.setCode(generateCode("PAY"));
        }
        if (providerOrderId != null) {
            payment.setProviderOrderId(providerOrderId);
        }
        if (providerRef != null) {
            payment.setProviderRef(providerRef);
        }
        paymentRepository.save(payment);
    }

    /** Hủy giữ ghế hoặc hủy đơn: trả lại ghế cho suất chiếu. */
    @Transactional
    public void cancel(User user, String bookingCode) {
        Booking booking = bookingRepository.findByCode(bookingCode)
                .orElseThrow(() -> NotFoundException.of("đơn đặt vé", bookingCode));

        if (!booking.getUser().getId().equals(user.getId()) && !user.isAdmin()) {
            throw new BadRequestException("Không có quyền hủy đơn đặt vé này");
        }

        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingIdOrderByIdAsc(
                booking.getId());
        bookingSeats.forEach(bookingSeat ->
                bookingSeat.setStatus(BookingSeat.STATUS_RELEASED));
        bookingSeatRepository.saveAll(bookingSeats);

        booking.setStatus(Booking.STATUS_CANCELLED);
        bookingRepository.save(booking);
    }

    /**
     * Giải phóng các booking pending quá hạn (ghế giữ lâu không thanh toán).
     * Được gọi định kỳ để trả ghế về cho suất chiếu.
     */
    @Scheduled(fixedDelayString = "${app.hold-expire-interval-ms:60000}")
    @Transactional
    public int releaseExpiredHolds() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(holdExpireMinutes);
        List<Booking> expired = bookingRepository
                .findByStatusAndCreatedAtBefore(Booking.STATUS_PENDING, deadline);

        for (Booking booking : expired) {
            List<BookingSeat> seats = bookingSeatRepository.findByBookingIdOrderByIdAsc(booking.getId());
            seats.forEach(seat -> seat.setStatus(BookingSeat.STATUS_RELEASED));
            bookingSeatRepository.saveAll(seats);
            booking.setStatus(Booking.STATUS_CANCELLED);
            bookingRepository.save(booking);
        }
        if (!expired.isEmpty()) {
            log.info("Đã giải phóng {} đơn giữ ghế quá hạn", expired.size());
        }
        return expired.size();
    }

    @Transactional(readOnly = true)
    public Booking getByCode(String code) {
        return bookingRepository.findByCode(code)
                .orElseThrow(() -> NotFoundException.of("đơn đặt vé", code));
    }

    @Transactional(readOnly = true)
    public List<Booking> findByUser(Integer userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public User requireUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.of("người dùng", userId));
    }

    private String generateCode(String prefix) {
        return codeGenerator.generate(prefix);
    }

    private String formatMoney(int amount) {
        return String.format("%,d đ", amount);
    }
}
