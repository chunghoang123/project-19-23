package com.cinex.service;

import com.cinex.domain.Movie;
import com.cinex.domain.Screen;
import com.cinex.domain.Seat;
import com.cinex.domain.Showtime;
import com.cinex.repository.MovieRepository;
import com.cinex.repository.ScreenRepository;
import com.cinex.repository.SeatRepository;
import com.cinex.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sinh bố cục ghế và lịch chiếu cho các phòng của chuỗi rạp.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShowtimeService {

    /** Các khung giờ chiếu cố định trong ngày. */
    private static final LocalTime[] SLOTS = {
            LocalTime.of(9, 0), LocalTime.of(11, 30), LocalTime.of(14, 0),
            LocalTime.of(16, 30), LocalTime.of(19, 0), LocalTime.of(21, 30)
    };

    private final ShowtimeRepository showtimeRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final MovieRepository movieRepository;

    /**
     * Tạo bố cục ghế cho một phòng: hàng A..H, ghế VIP ở 2 hàng cuối.
     */
    @Transactional
    public int createSeatsFor(Screen screen) {
        if (!seatRepository.findByScreenIdOrderByRowLabelAscSeatNumberAsc(screen.getId()).isEmpty()) {
            return 0;
        }

        int created = 0;
        int rows = Math.max(screen.getRowsCount(), 1);
        int cols = Math.max(screen.getColsCount(), 1);

        for (int r = 0; r < rows; r++) {
            String rowLabel = String.valueOf((char) ('A' + r));
            for (int c = 1; c <= cols; c++) {
                Seat seat = new Seat();
                seat.setScreen(screen);
                seat.setRowLabel(rowLabel);
                seat.setSeatNumber(c);
                seat.setSeatType(r >= rows - 2 ? Seat.TYPE_VIP : Seat.TYPE_STANDARD);
                seat.setIsActive(Boolean.TRUE);
                seatRepository.save(seat);
                created++;
            }
        }
        return created;
    }

    /**
     * Sinh lịch chiếu cho {@code daysAhead} ngày tới, mỗi phòng nhận một phần phim xoay vòng.
     */
    @Transactional
    public int generateShowtimes(int daysAhead, int moviesPerCinemaPerDay) {
        List<Movie> movies = movieRepository.findByStatusOrderByPopularityDesc(Movie.STATUS_ACTIVE);
        if (movies.isEmpty()) {
            return 0;
        }
        List<Screen> screens = screenRepository.findAll();
        if (screens.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        int created = 0;
        int movieCursor = 0;

        for (int day = 0; day < daysAhead; day++) {
            LocalDate date = today.plusDays(day);
            for (Screen screen : screens) {
                for (int m = 0; m < moviesPerCinemaPerDay; m++) {
                    Movie movie = movies.get((movieCursor + m) % movies.size());
                    // Chỉ chiếu phim đã phát hành hoặc sắp phát hành trong 7 ngày tới
                    if (movie.getReleaseDate() != null
                            && movie.getReleaseDate().isAfter(date.plusDays(7))) {
                        continue;
                    }
                    LocalTime slot = SLOTS[(day + m) % SLOTS.length];
                    LocalDateTime start = LocalDateTime.of(date, slot);
                    LocalDateTime end = start.plusMinutes(Math.max(movie.getDuration(), 60) + 15);

                    boolean conflict = showtimeRepository
                            .existsByScreenIdAndStartTimeGreaterThanAndStartTimeLessThan(
                                    screen.getId(), start.minusMinutes(30), end.plusMinutes(30));
                    if (conflict) {
                        continue;
                    }

                    Showtime showtime = new Showtime();
                    showtime.setMovie(movie);
                    showtime.setCinema(screen.getCinema());
                    showtime.setScreen(screen);
                    showtime.setStartTime(start);
                    showtime.setEndTime(end);
                    showtime.setBasePrice(movie.getTicketPrice());
                    showtime.setVipPrice(movie.getVipPrice());
                    showtime.setStatus(Showtime.STATUS_SCHEDULED);
                    showtimeRepository.save(showtime);
                    created++;
                }
                movieCursor++;
            }
        }
        log.info("Đã tạo {} suất chiếu cho {} ngày tới", created, daysAhead);
        return created;
    }

    @Transactional(readOnly = true)
    public List<Showtime> findUpcoming() {
        return showtimeRepository.findUpcoming(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Showtime> findByMovie(Integer movieId) {
        return showtimeRepository.findByMovieIdOrderByStartTimeAsc(movieId);
    }

    @Transactional(readOnly = true)
    public List<Showtime> findByCinema(Integer cinemaId) {
        return showtimeRepository.findByCinemaIdAndStartTimeAfterOrderByStartTimeAsc(
                cinemaId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<Showtime> findInRange(LocalDateTime from, LocalDateTime to) {
        return showtimeRepository.findInRange(from, to);
    }

    @Transactional(readOnly = true)
    public long countUpcoming() {
        return showtimeRepository.countByStartTimeAfter(LocalDateTime.now());
    }
}
