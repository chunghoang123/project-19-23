package com.cinex.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PromotionTest {

    private Promotion percentPromotion(int value, Integer maxDiscount, int minAmount) {
        Promotion promotion = new Promotion();
        promotion.setCode("TEST");
        promotion.setDiscountType(Promotion.TYPE_PERCENT);
        promotion.setDiscountValue(value);
        promotion.setMaxDiscount(maxDiscount);
        promotion.setMinAmount(minAmount);
        promotion.setIsActive(Boolean.TRUE);
        promotion.setStartDate(LocalDate.now().minusDays(1));
        promotion.setEndDate(LocalDate.now().plusDays(1));
        return promotion;
    }

    @Test
    @DisplayName("Chiết khấu phần trăm được tính đúng trên tổng tiền")
    void percentDiscount() {
        Promotion promotion = percentPromotion(10, null, 0);
        assertEquals(22500, promotion.calculateDiscount(225000));
    }

    @Test
    @DisplayName("Chiết khấu phần trăm không vượt quá mức tối đa")
    void percentDiscountCapped() {
        Promotion promotion = percentPromotion(20, 50000, 0);
        assertEquals(50000, promotion.calculateDiscount(500000));
    }

    @Test
    @DisplayName("Đơn dưới mức tối thiểu không được áp dụng khuyến mãi")
    void belowMinimumAmount() {
        Promotion promotion = percentPromotion(10, null, 200000);
        assertEquals(0, promotion.calculateDiscount(150000));
    }

    @Test
    @DisplayName("Chiết khấu cố định bị giới hạn bởi tổng tiền đơn hàng")
    void fixedDiscountNeverExceedsAmount() {
        Promotion promotion = new Promotion();
        promotion.setCode("FIXED");
        promotion.setDiscountType(Promotion.TYPE_FIXED);
        promotion.setDiscountValue(50000);
        promotion.setMinAmount(0);
        promotion.setIsActive(Boolean.TRUE);
        assertEquals(30000, promotion.calculateDiscount(30000));
    }

    @Test
    @DisplayName("Mã đã hết hạn hoặc tạm dừng không được dùng")
    void expiredOrInactive() {
        Promotion expired = percentPromotion(10, null, 0);
        expired.setEndDate(LocalDate.now().minusDays(1));
        assertFalse(expired.isUsableOn(LocalDate.now()));

        Promotion inactive = percentPromotion(10, null, 0);
        inactive.setIsActive(Boolean.FALSE);
        assertFalse(inactive.isUsableOn(LocalDate.now()));

        assertTrue(percentPromotion(10, null, 0).isUsableOn(LocalDate.now()));
    }

    @Test
    @DisplayName("Mã giới hạn số lượt dùng")
    void usageLimit() {
        Promotion promotion = percentPromotion(10, null, 0);
        promotion.setUsageLimit(1);
        promotion.setUsedCount(1);
        assertFalse(promotion.isUsableOn(LocalDate.now()));
    }
}
