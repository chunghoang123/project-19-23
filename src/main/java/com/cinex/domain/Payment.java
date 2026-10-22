package com.cinex.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment {

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_PAID = "paid";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_REFUNDED = "refunded";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 30)
    private String provider = "cash";

    @Column(nullable = false)
    private Integer amount = 0;

    @Column(nullable = false, length = 20)
    private String status = STATUS_PENDING;

    @Column(name = "provider_order_id", length = 80)
    private String providerOrderId;

    @Column(name = "provider_ref", length = 120)
    private String providerRef;

    /** Link trang thanh toán của nhà cung cấp (PayOS). */
    @Column(name = "checkout_url", length = 500)
    private String checkoutUrl;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
