package com.cinex.repository;

import com.cinex.domain.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

    Optional<Promotion> findByCodeIgnoreCase(String code);

    List<Promotion> findByIsActiveTrueOrderByDiscountValueDesc();

    List<Promotion> findAllByOrderByIdDesc();
}
