package com.cinex.service;

import com.cinex.domain.Movie;
import com.cinex.dto.ShowtimeDayGroup;
import com.cinex.exception.NotFoundException;
import com.cinex.repository.MovieRepository;
import com.cinex.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;

    @Transactional(readOnly = true)
    public List<Movie> findNowShowing() {
        return movieRepository.findNowShowing();
    }

    @Transactional(readOnly = true)
    public List<Movie> findUpcoming() {
        return movieRepository.findTop8ByStatusAndReleaseDateGreaterThanEqualOrderByPopularityDesc(
                Movie.STATUS_ACTIVE, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Movie> findHot() {
        return movieRepository.findTop12ByStatusOrderByPopularityDesc(Movie.STATUS_ACTIVE);
    }

    @Transactional(readOnly = true)
    public Page<Movie> search(String keyword, int page, int size) {
        String normalized = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        return movieRepository.search(normalized,
                PageRequest.of(Math.max(page, 0), size, Sort.by("popularity").descending()));
    }

    @Transactional(readOnly = true)
    public Movie getById(Integer id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("phim", id));
    }

    /** Nhóm suất chiếu theo ngày cho trang lịch chiếu. */
    @Transactional(readOnly = true)
    public List<ShowtimeDayGroup> groupShowtimesByDay(List<com.cinex.domain.Showtime> showtimes) {
        Map<LocalDate, List<com.cinex.domain.Showtime>> byDate = new LinkedHashMap<>();
        for (com.cinex.domain.Showtime showtime : showtimes) {
            byDate.computeIfAbsent(showtime.getStartTime().toLocalDate(),
                    key -> new ArrayList<>()).add(showtime);
        }
        List<ShowtimeDayGroup> groups = new ArrayList<>();
        byDate.forEach((date, items) -> {
            List<Integer> cinemaIds = new ArrayList<>();
            for (com.cinex.domain.Showtime showtime : items) {
                Integer cinemaId = showtime.getCinema().getId();
                if (!cinemaIds.contains(cinemaId)) {
                    cinemaIds.add(cinemaId);
                }
            }
            groups.add(ShowtimeDayGroup.builder()
                    .date(date)
                    .showtimes(items)
                    .cinemaIds(cinemaIds)
                    .build());
        });
        return groups;
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return movieRepository.countByStatus(Movie.STATUS_ACTIVE);
    }
}
