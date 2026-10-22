package com.cinex.repository;

import com.cinex.domain.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Integer> {

    /** Tìm phim đã đồng bộ từ TMDB theo mã phim của TMDB. */
    Optional<Movie> findByTmdbId(Integer tmdbId);

    List<Movie> findByStatusOrderByPopularityDesc(String status);

    List<Movie> findTop12ByStatusOrderByPopularityDesc(String status);

    List<Movie> findTop8ByStatusAndReleaseDateGreaterThanEqualOrderByPopularityDesc(String status,
                                                                                  LocalDate from);

    List<Movie> findTop8ByStatusAndReleaseDateLessThanEqualOrderByReleaseDateDesc(String status,
                                                                                    LocalDate to);

    /** Phim mới ra rạp nhất, không phụ thuộc ngày phát hành. */
    List<Movie> findTop8ByStatusOrderByReleaseDateDesc(String status);

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

    /** Phim đang chiếu, mới ra rạp nhất lên đầu. */
    @Query("""
            SELECT m FROM Movie m
            WHERE m.status = 'active'
              AND EXISTS (
                  SELECT s FROM Showtime s
                  WHERE s.movie = m AND s.startTime >= CURRENT_TIMESTAMP AND s.status = 'scheduled'
              )
            ORDER BY m.releaseDate DESC, m.popularity DESC
            """)
    List<Movie> findNowShowingByNewest();

    long countByStatus(String status);
}
