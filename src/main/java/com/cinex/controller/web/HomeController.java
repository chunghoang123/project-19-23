package com.cinex.controller.web;

import com.cinex.domain.Cinema;
import com.cinex.domain.Movie;
import com.cinex.dto.ShowtimeDayGroup;
import com.cinex.service.CinemaService;
import com.cinex.service.MovieService;
import com.cinex.service.PromotionService;
import com.cinex.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MovieService movieService;
    private final CinemaService cinemaService;
    private final PromotionService promotionService;
    private final ShowtimeService showtimeService;

    @GetMapping("/")
    public String home(Model model) {
        List<Movie> nowShowing = movieService.findNowShowing();
        List<Movie> upcoming = movieService.findUpcoming();
        List<Cinema> cinemas = cinemaService.findAllActive();

        LocalDateTime now = LocalDateTime.now();
        List<com.cinex.domain.Showtime> today = showtimeService.findUpcoming().stream()
                .filter(s -> s.getStartTime().toLocalDate().isEqual(LocalDate.now()))
                .limit(12)
                .toList();

        model.addAttribute("nowShowing", nowShowing);
        model.addAttribute("upcoming", upcoming);
        model.addAttribute("cinemas", cinemas);
        // Chỉ lấy 12 rạp đầu để hiển thị - giới hạn ở tầng Java,
        // không dùng #lists.listSubList (không tồn tại trong Thymeleaf 3.1).
        model.addAttribute("cinemasTop", cinemas.stream().limit(12).toList());
        model.addAttribute("promotions", promotionService.findActive());
        model.addAttribute("todayShowtimes", today);
        model.addAttribute("now", now);
        return "home";
    }
}
