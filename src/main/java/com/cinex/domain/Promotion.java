package com.cinex.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
public class Promotion {

    public static final String TYPE_PERCENT = "percent";
    public static final String TYPE_FIXED = "fixed";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "discount_type", nullable = false, length = 20)
    private String discountType = TYPE_PERCENT;

    @Column(name = "discount_value", nullable = false)
    private Integer discountValue = 0;

    @Column(name = "max_discount")
    private Integer maxDiscount;

    @Column(name = "min_amount", nullable = false)
    private Integer minAmount = 0;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /** Mức giảm cho đơn hàng {@code amount}, không vượt quá giới hạn. */
    public int calculateDiscount(int amount) {
        if (!isUsableOn(LocalDate.now()) || amount < minAmount) {
            return 0;
        }
        int discount = TYPE_FIXED.equalsIgnoreCase(discountType)
                ? discountValue
                : (int) Math.round(amount * discountValue / 100.0);
        if (maxDiscount != null && maxDiscount > 0) {
            discount = Math.min(discount, maxDiscount);
        }
        return Math.min(discount, amount);
    }

    public boolean isUsableOn(LocalDate date) {
        if (!Boolean.TRUE.equals(isActive)) {
            return false;
        }
        if (startDate != null && date.isBefore(startDate)) {
            return false;
        }
        if (endDate != null && date.isAfter(endDate)) {
            return false;
        }
        return usageLimit == null || usedCount < usageLimit;
    }
}
