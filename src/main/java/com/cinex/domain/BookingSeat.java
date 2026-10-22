package com.cinex.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Ghế đã được giữ/đặt cho một suất chiếu cụ thể.
 * Duy nhất theo (showtime, seat) nên không thể bán trùng ghế cho cùng suất.
 */
@Entity
@Table(name = "booking_seats",
        uniqueConstraints = @UniqueConstraint(name = "uk_booking_seats_showtime_seat",
                columnNames = {"showtime_id", "seat_id"}))
@Getter
@Setter
@NoArgsConstructor
public class BookingSeat {

    public static final String STATUS_RESERVED = "reserved";
    public static final String STATUS_PAID = "paid";
    public static final String STATUS_RELEASED = "released";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(name = "seat_label", nullable = false, length = 10)
    private String seatLabel;

    @Column(nullable = false)
    private Integer price = 0;

    @Column(nullable = false, length = 20)
    private String status = STATUS_RESERVED;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
