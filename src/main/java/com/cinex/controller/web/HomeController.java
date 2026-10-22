package com.cinex.controller.web;

import com.cinex.domain.Movie;
import com.cinex.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    /** Số phim đề xuất hiển thị trên trang chủ. */
    private static final int RECOMMENDED_LIMIT = 10;

    /** Số phim xoay vòng trong phần giới thiệu (hero). */
    private static final int FEATURED_LIMIT = 5;

    private final MovieService movieService;

    @GetMapping("/")
    public String home(Model model) {
        List<Movie> featured = movieService.findForHero(FEATURED_LIMIT);
        List<Movie> recommended = movieService.findRecommended(RECOMMENDED_LIMIT);

        model.addAttribute("featured", featured);
        model.addAttribute("recommended", recommended);
        model.addAttribute("now", LocalDateTime.now());
        return "home";
    }
}
