package com.cinex.controller.web;

import com.cinex.service.CinemaService;
import com.cinex.service.MovieService;
import com.cinex.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;
    private final MovieService movieService;
    private final CinemaService cinemaService;

    /**
     * Lịch chiếu theo ngày. Mặc định hiển thị ngày hôm nay; có thể chọn ngày khác
     * hoặc xem trong khoảng {@code days} ngày tới để giữ trang nhẹ.
     */
    @GetMapping("/showtimes")
    public String schedule(@RequestParam(required = false) Integer cinemaId,
                           @RequestParam(required = false) LocalDate date,
                           @RequestParam(defaultValue = "1") int days,
                           Model model) {
        LocalDate from = date != null ? date : LocalDate.now();
        int span = Math.min(Math.max(days, 1), 7);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = from.plusDays(span).atStartOfDay();

        var showtimes = showtimeService.findInRange(start, end).stream()
                .filter(s -> cinemaId == null || s.getCinema().getId().equals(cinemaId))
                .toList();

        model.addAttribute("groups", movieService.groupShowtimesByDay(showtimes));
        model.addAttribute("cinemas", cinemaService.findAllActive());
        model.addAttribute("selectedCinemaId", cinemaId);
        model.addAttribute("fromDate", from);
        model.addAttribute("days", span);
        model.addAttribute("now", LocalDateTime.now());
        return "showtimes/schedule";
    }
}
