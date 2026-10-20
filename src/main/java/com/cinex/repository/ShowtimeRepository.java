package com.cinex.repository;

import com.cinex.domain.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShowtimeRepository extends JpaRepository<Showtime, Integer> {

    List<Showtime> findByMovieIdOrderByStartTimeAsc(Integer movieId);

    List<Showtime> findByCinemaIdAndStartTimeBetweenOrderByStartTimeAsc(Integer cinemaId,
                                                                        LocalDateTime from,
                                                                        LocalDateTime to);

    List<Showtime> findByMovieIdAndStartTimeBetweenOrderByStartTimeAsc(Integer movieId,
                                                                        LocalDateTime from,
                                                                        LocalDateTime to);

    List<Showtime> findByCinemaIdAndStartTimeAfterOrderByStartTimeAsc(Integer cinemaId,
                                                                       LocalDateTime after);

    Optional<Showtime> findByScreenIdAndStartTime(Integer screenId, LocalDateTime startTime);

    boolean existsByScreenIdAndStartTimeGreaterThanAndStartTimeLessThan(
            Integer screenId, LocalDateTime from, LocalDateTime to);

    @Query("""
            SELECT s FROM Showtime s
            WHERE s.status = 'scheduled' AND s.startTime >= :from
            ORDER BY s.startTime ASC
            """)
    List<Showtime> findUpcoming(@Param("from") LocalDateTime from);

    long countByStartTimeAfter(LocalDateTime after);

    /** Tổng số suất chiếu theo ngày, dùng cho biểu đồ doanh thu của admin. */
    @Query("""
            SELECT s FROM Showtime s
            WHERE s.startTime >= :from AND s.startTime < :to AND s.status = 'scheduled'
            ORDER BY s.startTime ASC
            """)
    List<Showtime> findInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
