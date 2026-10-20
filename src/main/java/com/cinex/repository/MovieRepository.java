package com.cinex.repository;

import com.cinex.domain.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Integer> {

    List<Movie> findByStatusOrderByPopularityDesc(String status);

    List<Movie> findTop12ByStatusOrderByPopularityDesc(String status);

    List<Movie> findTop8ByStatusAndReleaseDateGreaterThanEqualOrderByPopularityDesc(String status,
                                                                                  LocalDate from);

    List<Movie> findTop8ByStatusAndReleaseDateLessThanEqualOrderByReleaseDateDesc(String status,
                                                                                    LocalDate to);

    List<Movie> findByTitleContainingIgnoreCaseOrderByPopularityDesc(String keyword);

    @Query("""
            SELECT m FROM Movie m
            WHERE m.status = 'active'
              AND (:keyword IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(m.genre) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Movie> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT m FROM Movie m
            WHERE m.status = 'active'
              AND EXISTS (
                  SELECT s FROM Showtime s
                  WHERE s.movie = m AND s.startTime >= CURRENT_TIMESTAMP AND s.status = 'scheduled'
              )
            ORDER BY m.popularity DESC
            """)
    List<Movie> findNowShowing();

    long countByStatus(String status);
}
