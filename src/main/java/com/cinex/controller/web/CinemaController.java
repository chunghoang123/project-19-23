package com.cinex.controller.web;

import com.cinex.domain.Cinema;
import com.cinex.service.CinemaService;
import com.cinex.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CinemaController {

    private final CinemaService cinemaService;
    private final ShowtimeService showtimeService;

    @GetMapping("/cinemas")
    public String list(Model model) {
        Map<String, java.util.List<Cinema>> grouped = cinemaService.groupByProvince();
        model.addAttribute("grouped", grouped);
        model.addAttribute("total", cinemaService.countActive());
        return "cinemas/list";
    }

    @GetMapping("/cinemas/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Cinema cinema = cinemaService.getById(id);
        model.addAttribute("cinema", cinema);
        model.addAttribute("showtimes", showtimeService.findByCinema(id));
        return "cinemas/detail";
    }
}
