package com.cinex.service;

import com.cinex.domain.Promotion;
import com.cinex.exception.BadRequestException;
import com.cinex.exception.NotFoundException;
import com.cinex.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;

    @Transactional(readOnly = true)
    public List<Promotion> findActive() {
        return promotionRepository.findByIsActiveTrueOrderByDiscountValueDesc().stream()
                .filter(promotion -> promotion.isUsableOn(LocalDate.now()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Promotion> findAll() {
        return promotionRepository.findAllByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public Promotion getById(Integer id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("khuyến mãi", id));
    }

    @Transactional
    public Promotion create(Promotion promotion) {
        if (promotion.getCode() == null || promotion.getCode().isBlank()) {
            throw new BadRequestException("Mã khuyến mãi không được để trống");
        }
        promotionRepository.findByCodeIgnoreCase(promotion.getCode().trim()).ifPresent(existing -> {
            throw new BadRequestException("Mã khuyến mãi đã tồn tại");
        });
        promotion.setCode(promotion.getCode().trim().toUpperCase());
        return promotionRepository.save(promotion);
    }

    @Transactional
    public Promotion update(Integer id, Promotion payload) {
        Promotion promotion = getById(id);
        promotion.setTitle(payload.getTitle());
        promotion.setDescription(payload.getDescription());
        promotion.setDiscountType(payload.getDiscountType());
        promotion.setDiscountValue(payload.getDiscountValue());
        promotion.setMaxDiscount(payload.getMaxDiscount());
        promotion.setMinAmount(payload.getMinAmount());
        promotion.setStartDate(payload.getStartDate());
        promotion.setEndDate(payload.getEndDate());
        promotion.setUsageLimit(payload.getUsageLimit());
        promotion.setIsActive(payload.getIsActive());
        return promotionRepository.save(promotion);
    }

    @Transactional
    public void delete(Integer id) {
        promotionRepository.delete(getById(id));
    }
}
