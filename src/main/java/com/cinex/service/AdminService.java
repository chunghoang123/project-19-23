package com.cinex.service;

import com.cinex.domain.Booking;
import com.cinex.dto.AdminStats;
import com.cinex.dto.BrandRevenue;
import com.cinex.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;
    private final ScreenRepository screenRepository;
    private final ShowtimeRepository showtimeRepository;
    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public AdminStats collectStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekStart = now.toLocalDate().with(java.time.DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime lastWeekStart = weekStart.minusWeeks(1);

        long revenueThisWeek = bookingRepository.sumRevenueInRange(weekStart, now);
        long revenueLastWeek = bookingRepository.sumRevenueInRange(lastWeekStart, weekStart);

        List<Long> revenueDaily = new ArrayList<>();
        for (int day = 0; day < 7; day++) {
            LocalDateTime from = weekStart.plusDays(day);
            revenueDaily.add(bookingRepository.sumRevenueInRange(from, from.plusDays(1)));
        }

        Map<String, long[]> byBrand = new LinkedHashMap<>();
        byBrand.put("CineX", new long[]{0, 0});
        for (Booking booking : bookingRepository.findInRange(lastWeekStart, now)) {
            String brand = booking.getShowtime().getCinema().getBrand();
            long[] bucket = byBrand.computeIfAbsent(brand, key -> new long[]{0, 0});
            bucket[0] += booking.getFinalAmount();
            bucket[1]++;
        }
        List<BrandRevenue> brandRevenue = byBrand.entrySet().stream()
                .map(entry -> BrandRevenue.builder()
                        .brand(entry.getKey())
                        .revenue(entry.getValue()[0])
                        .bookings(entry.getValue()[1])
                        .build())
                .toList();

        return AdminStats.builder()
                .totalMovies(movieRepository.count())
                .activeMovies(movieRepository.countByStatus("active"))
                .totalCinemas(cinemaRepository.countByStatus("active"))
                .totalScreens(screenRepository.count())
                .upcomingShowtimes(showtimeRepository.countByStartTimeAfter(now))
                .totalBookings(bookingRepository.count())
                .pendingBookings(bookingRepository.countByStatus(Booking.STATUS_PENDING))
                .revenueThisWeek(revenueThisWeek)
                .revenueLastWeek(revenueLastWeek)
                .revenueDaily(revenueDaily)
                .brandRevenue(brandRevenue)
                .build();
    }
}
