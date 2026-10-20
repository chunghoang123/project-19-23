package com.cinex.controller.web;

import com.cinex.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @GetMapping("/promotions")
    public String list(Model model) {
        model.addAttribute("promotions", promotionService.findActive());
        return "promotions";
    }
}
