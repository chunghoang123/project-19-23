package com.cinex.service;

import com.cinex.domain.*;
import com.cinex.dto.CheckoutForm;
import com.cinex.exception.BadRequestException;
import com.cinex.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceTest {

    @Mock BookingRepository bookingRepository;
    @Mock BookingSeatRepository bookingSeatRepository;
    @Mock SeatRepository seatRepository;
    @Mock ShowtimeRepository showtimeRepository;
    @Mock TicketRepository ticketRepository;
    @Mock BillRepository billRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock PromotionRepository promotionRepository;
    @Mock UserRepository userRepository;
    @Mock CodeGenerator codeGenerator;

    @InjectMocks BookingService bookingService;

    @BeforeEach
    void stubCodes() {
        when(codeGenerator.generate(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0) + "260101000000" + UUID.randomUUID());
    }

    private Screen screen() {
        Screen screen = new Screen();
        screen.setId(1);
        screen.setName("Rạp 1");
        return screen;
    }

    private Seat seat(int id, String label) {
        Seat seat = new Seat();
        seat.setId(id);
        seat.setScreen(screen());
        seat.setRowLabel(label.substring(0, 1));
        seat.setSeatNumber(Integer.parseInt(label.substring(1)));
        seat.setSeatType(Seat.TYPE_STANDARD);
        return seat;
    }

    private Showtime showtime() {
        Showtime showtime = new Showtime();
        showtime.setId(10);
        showtime.setScreen(screen());
        showtime.setStartTime(LocalDateTime.now().plusHours(3));
        showtime.setBasePrice(75000);
        showtime.setVipPrice(120000);
        return showtime;
    }

    private User user() {
        User user = new User();
        user.setId(1);
        user.setName("Nguyễn Văn A");
        user.setEmail("user@cinex.vn");
        return user;
    }

    @Test
    @DisplayName("Giữ ghế: từ chối khi ghế đã có người giữ hoặc đã bán")
    void holdSeatsRejectsTakenSeats() {
        when(showtimeRepository.findById(10)).thenReturn(Optional.of(showtime()));
        when(seatRepository.findAllById(any())).thenReturn(List.of(seat(1, "A1")));
        when(bookingSeatRepository.findTakenSeatIds(10)).thenReturn(List.of(1));

        CheckoutForm form = new CheckoutForm();
        form.setShowtimeId(10);
        form.setSeatIds(List.of(1));

        assertThatThrownBy(() -> bookingService.holdSeats(user(), form))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("A1");
    }

    @Test
    @DisplayName("Giữ ghế: từ chối khi suất chiếu đã bắt đầu")
    void holdSeatsRejectsStartedShowtime() {
        Showtime started = showtime();
        started.setStartTime(LocalDateTime.now().minusMinutes(10));
        when(showtimeRepository.findById(10)).thenReturn(Optional.of(started));

        CheckoutForm form = new CheckoutForm();
        form.setShowtimeId(10);
        form.setSeatIds(List.of(1));

        assertThatThrownBy(() -> bookingService.holdSeats(user(), form))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("đã bắt đầu");
    }

    @Test
    @DisplayName("Khuyến mãi: đơn dưới mức tối thiểu thì báo lỗi, không tăng lượt dùng")
    void confirmBookingRejectsPromotionBelowMinimum() {
        Booking booking = new Booking();
        booking.setId(1);
        booking.setCode("BK0001");
        booking.setUser(user());
        booking.setStatus(Booking.STATUS_PENDING);
        booking.setShowtime(showtime());

        BookingSeat bookingSeat = new BookingSeat();
        bookingSeat.setPrice(75000);
        bookingSeat.setSeatLabel("A1");
        bookingSeat.setStatus(BookingSeat.STATUS_RESERVED);

        Promotion promotion = new Promotion();
        promotion.setId(1);
        promotion.setCode("CINEX10");
        promotion.setDiscountType(Promotion.TYPE_PERCENT);
        promotion.setDiscountValue(10);
        promotion.setMinAmount(200000);
        promotion.setIsActive(Boolean.TRUE);
        promotion.setStartDate(LocalDate.now().minusDays(1));
        promotion.setEndDate(LocalDate.now().plusDays(30));

        when(bookingRepository.findByCode("BK0001")).thenReturn(Optional.of(booking));
        when(bookingSeatRepository.findByBookingIdOrderByIdAsc(1)).thenReturn(List.of(bookingSeat));
        when(promotionRepository.findByCodeIgnoreCase("CINEX10")).thenReturn(Optional.of(promotion));

        assertThatThrownBy(() -> bookingService.confirmBooking(user(), "BK0001", "CINEX10", "cash"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("200,000");

        verify(promotionRepository, never()).incrementUsedCount(anyInt());
        verify(promotionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Khuyến mãi: đơn đủ hạn mức thì áp dụng và tăng lượt dùng")
    void confirmBookingAppliesValidPromotion() {
        Booking booking = new Booking();
        booking.setId(1);
        booking.setCode("BK0002");
        booking.setUser(user());
        booking.setStatus(Booking.STATUS_PENDING);
        booking.setShowtime(showtime());

        BookingSeat seat1 = new BookingSeat();
        seat1.setPrice(75000);
        seat1.setSeatLabel("A1");
        seat1.setStatus(BookingSeat.STATUS_RESERVED);
        BookingSeat seat2 = new BookingSeat();
        seat2.setPrice(75000);
        seat2.setSeatLabel("A2");
        seat2.setStatus(BookingSeat.STATUS_RESERVED);

        Promotion promotion = new Promotion();
        promotion.setId(1);
        promotion.setCode("CINEX10");
        promotion.setDiscountType(Promotion.TYPE_PERCENT);
        promotion.setDiscountValue(10);
        promotion.setMaxDiscount(50000);
        promotion.setMinAmount(100000);
        promotion.setIsActive(Boolean.TRUE);
        promotion.setStartDate(LocalDate.now().minusDays(1));
        promotion.setEndDate(LocalDate.now().plusDays(30));

        when(bookingRepository.findByCode("BK0002")).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingSeatRepository.findByBookingIdOrderByIdAsc(1)).thenReturn(List.of(seat1, seat2));
        when(promotionRepository.findByCodeIgnoreCase("CINEX10")).thenReturn(Optional.of(promotion));

        Booking result = bookingService.confirmBooking(user(), "BK0002", "CINEX10", "card");

        assertThat(result.getTotalAmount()).isEqualTo(150000);
        assertThat(result.getDiscountAmount()).isEqualTo(15000);
        assertThat(result.getFinalAmount()).isEqualTo(135000);
        assertThat(result.getPromotionCode()).isEqualTo("CINEX10");
        assertThat(result.getStatus()).isEqualTo(Booking.STATUS_CONFIRMED);
        verify(promotionRepository).incrementUsedCount(1);
    }

    @Test
    @DisplayName("Mã khuyến mãi không tồn tại thì báo lỗi rõ ràng")
    void confirmBookingRejectsUnknownPromotion() {
        Booking booking = new Booking();
        booking.setId(1);
        booking.setCode("BK0003");
        booking.setUser(user());
        booking.setStatus(Booking.STATUS_PENDING);
        booking.setShowtime(showtime());

        BookingSeat bookingSeat = new BookingSeat();
        bookingSeat.setPrice(75000);
        bookingSeat.setSeatLabel("A1");

        when(bookingRepository.findByCode("BK0003")).thenReturn(Optional.of(booking));
        when(bookingSeatRepository.findByBookingIdOrderByIdAsc(1)).thenReturn(List.of(bookingSeat));
        when(promotionRepository.findByCodeIgnoreCase(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.confirmBooking(user(), "BK0003", "SAI", "cash"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("không tồn tại");
    }
}
