package com.cinex.repository;

import com.cinex.domain.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

    Optional<Promotion> findByCodeIgnoreCase(String code);

    List<Promotion> findByIsActiveTrueOrderByDiscountValueDesc();

    List<Promotion> findAllByOrderByIdDesc();

    /** Tăng lượt dùng của mã khuyến mãi, tránh mất cập nhật khi có nhiều request đồng thời. */
    @Modifying
    @Query("UPDATE Promotion p SET p.usedCount = p.usedCount + 1 WHERE p.id = :id")
    int incrementUsedCount(@Param("id") Integer id);
}
