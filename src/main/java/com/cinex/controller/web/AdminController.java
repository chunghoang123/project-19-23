package com.cinex.controller.web;

import com.cinex.domain.Booking;
import com.cinex.config.IntegrationProperties;
import com.cinex.dto.AdminStats;
import com.cinex.repository.BookingRepository;
import com.cinex.service.AdminService;
import com.cinex.service.CinemaService;
import com.cinex.service.MovieService;
import com.cinex.service.PromotionService;
import com.cinex.service.ShowtimeService;
import com.cinex.service.TmdbService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final MovieService movieService;
    private final CinemaService cinemaService;
    private final PromotionService promotionService;
    private final TmdbService tmdbService;
    private final ShowtimeService showtimeService;
    private final IntegrationProperties integrationProperties;
    private final BookingRepository bookingRepository;

    /** Số ngày phát hành suất chiếu cho phim mới. */
    @Value("${app.showtime-days-ahead:7}")
    private int showtimeDaysAhead;

    @GetMapping("/admin")
    public String dashboard(Model model) {
        AdminStats stats = adminService.collectStats();
        model.addAttribute("stats", stats);
        model.addAttribute("recentBookings", bookingRepository.findTop10ByOrderByCreatedAtDesc());
        return "admin/dashboard";
    }

    @GetMapping("/admin/movies")
    public String movies(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<com.cinex.domain.Movie> movies = movieService.search(null, page, 20);
        model.addAttribute("movies", movies);
        model.addAttribute("tmdbEnabled", integrationProperties.isTmdbReady());
        return "admin/movies";
    }

    /** Đồng bộ danh sách phim từ TMDB về CSDL rồi phát hành suất chiếu cho phim mới. */
    @PostMapping("/admin/movies/sync-tmdb")
    public String syncTmdb(@RequestParam(defaultValue = "popular") String source,
                           @RequestParam(defaultValue = "1") int pages,
                           RedirectAttributes redirectAttributes) {
        TmdbService.SyncResult result = tmdbService.sync(source, pages);
        int showtimes = showtimeService.generateShowtimesForNewMovies(showtimeDaysAhead, 3);
        redirectAttributes.addFlashAttribute("success",
                result.summary() + (showtimes > 0 ? " — đã phát hành " + showtimes + " suất chiếu" : ""));
        return "redirect:/admin/movies";
    }

    @GetMapping("/admin/cinemas")
    public String cinemas(Model model) {
        model.addAttribute("grouped", cinemaService.groupByProvince());
        return "admin/cinemas";
    }

    @GetMapping("/admin/bookings")
    public String bookings(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Booking> bookings = bookingRepository.findAll(
                PageRequest.of(Math.max(page, 0), 20));
        model.addAttribute("bookings", bookings);
        return "admin/bookings";
    }

    @GetMapping("/admin/promotions")
    public String promotions(Model model) {
        model.addAttribute("promotions", promotionService.findAll());
        return "admin/promotions";
    }

    @GetMapping("/admin/promotions/{id}")
    public String promotionEdit(@PathVariable Integer id, Model model) {
        model.addAttribute("promotion", promotionService.getById(id));
        return "admin/promotion-form";
    }
}
