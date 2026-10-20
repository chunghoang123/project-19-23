package com.cinex.service;

import com.cinex.domain.*;
import com.cinex.dto.CheckoutForm;
import com.cinex.exception.BadRequestException;
import com.cinex.exception.NotFoundException;
import com.cinex.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final DateTimeFormatter CODE_TIME = DateTimeFormatter.ofPattern("yyMMddHHmmss");
    private static final Random RANDOM = new Random();

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final TicketRepository ticketRepository;
    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final PromotionRepository promotionRepository;
    private final UserRepository userRepository;

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
            bookingSeatRepository.save(bookingSeat);
        }

        booking.setTotalAmount(total);
        booking.setFinalAmount(total);
        return bookingRepository.save(booking);
    }

    /** Xác nhận thanh toán: chuyển booking sang confirmed, sinh vé và hoá đơn. */
    @Transactional
    public Booking confirmBooking(User user, String bookingCode, String promotionCode,
                                  String paymentMethod) {
        Booking booking = bookingRepository.findByCode(bookingCode)
                .orElseThrow(() -> NotFoundException.of("đơn đặt vé", bookingCode));

        if (!booking.getUser().getId().equals(user.getId()) && !user.isAdmin()) {
            throw new BadRequestException("Không có quyền xác nhận đơn đặt vé này");
        }
        if (!Booking.STATUS_PENDING.equals(booking.getStatus())) {
            throw new BadRequestException("Đơn đặt vé đã được xử lý");
        }

        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingIdOrderByIdAsc(
                booking.getId());
        if (bookingSeats.isEmpty()) {
            throw new BadRequestException("Đơn đặt vé không có ghế nào");
        }

        int subtotal = bookingSeats.stream().mapToInt(BookingSeat::getPrice).sum();
        int discount = 0;
        if (promotionCode != null && !promotionCode.isBlank()) {
            Promotion promotion = promotionRepository.findByCodeIgnoreCase(promotionCode.trim())
                    .orElseThrow(() -> new BadRequestException("Mã khuyến mãi không tồn tại"));
            if (!promotion.isUsableOn(LocalDateTime.now().toLocalDate())) {
                throw new BadRequestException("Mã khuyến mãi không còn hiệu lực");
            }
            discount = promotion.calculateDiscount(subtotal);
            promotion.setUsedCount(promotion.getUsedCount() + 1);
            promotionRepository.save(promotion);
            booking.setPromotionCode(promotion.getCode());
        }

        booking.setTotalAmount(subtotal);
        booking.setDiscountAmount(discount);
        booking.setFinalAmount(subtotal - discount);
        booking.setPaymentMethod(paymentMethod == null ? "cash" : paymentMethod);
        booking.setStatus(Booking.STATUS_CONFIRMED);
        bookingRepository.save(booking);

        bookingSeats.forEach(bookingSeat ->
                bookingSeat.setStatus(BookingSeat.STATUS_PAID));
        bookingSeatRepository.saveAll(bookingSeats);

        for (BookingSeat bookingSeat : bookingSeats) {
            Ticket ticket = new Ticket();
            ticket.setTicketCode(generateCode("TK"));
            ticket.setBooking(booking);
            ticket.setUser(user);
            ticket.setShowtime(booking.getShowtime());
            ticket.setSeatLabel(bookingSeat.getSeatLabel());
            ticket.setQrCode(booking.getCode() + "-" + bookingSeat.getSeatLabel());
            ticket.setStatus(Ticket.STATUS_ISSUED);
            ticketRepository.save(ticket);
        }

        Bill bill = new Bill();
        bill.setCode(generateCode("BL"));
        bill.setBooking(booking);
        bill.setUser(user);
        bill.setSubtotal(subtotal);
        bill.setDiscount(discount);
        bill.setTotal(subtotal - discount);
        billRepository.save(bill);

        Payment payment = new Payment();
        payment.setCode(generateCode("PAY"));
        payment.setBooking(booking);
        payment.setUser(user);
        payment.setProvider(booking.getPaymentMethod());
        payment.setAmount(booking.getFinalAmount());
        payment.setStatus(Payment.STATUS_PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        return booking;
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

    /** Giải phóng các booking pending quá hạn (ghế giữ lâu không thanh toán). */
    @Transactional
    public int releaseExpiredHolds() {
        List<Booking> expired = bookingRepository.findAll().stream()
                .filter(booking -> Booking.STATUS_PENDING.equals(booking.getStatus()))
                .filter(booking -> booking.getCreatedAt() != null)
                .filter(booking -> booking.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(15)))
                .toList();

        for (Booking booking : expired) {
            List<BookingSeat> seats = bookingSeatRepository.findByBookingIdOrderByIdAsc(booking.getId());
            seats.forEach(seat -> seat.setStatus(BookingSeat.STATUS_RELEASED));
            bookingSeatRepository.saveAll(seats);
            booking.setStatus(Booking.STATUS_CANCELLED);
            bookingRepository.save(booking);
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
        return prefix + LocalDateTime.now().format(CODE_TIME) + RANDOM.nextInt(1000, 9999);
    }
}
