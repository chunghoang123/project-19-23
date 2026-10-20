package com.cinex.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ghế thuộc bố cục cố định của một phòng.
 * Trạng thái đã bán/đang giữ được quản lý ở bảng {@link BookingSeat} theo từng suất chiếu.
 */
@Entity
@Table(name = "seats",
        uniqueConstraints = @UniqueConstraint(name = "idx_seats_screen_row_number",
                columnNames = {"screen_id", "row_label", "seat_number"}))
@Getter
@Setter
@NoArgsConstructor
public class Seat {

    public static final String TYPE_STANDARD = "standard";
    public static final String TYPE_VIP = "vip";
    public static final String TYPE_COUPLE = "couple";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @Column(name = "row_label", nullable = false, length = 5)
    private String rowLabel;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Column(name = "seat_type", nullable = false, length = 20)
    private String seatType = TYPE_STANDARD;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    /** Nhãn hiển thị, ví dụ {@code A5}. */
    public String getLabel() {
        return rowLabel + seatNumber;
    }
}
