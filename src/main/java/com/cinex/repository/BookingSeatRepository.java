package com.cinex.repository;

import com.cinex.domain.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Integer> {

    List<BookingSeat> findByShowtimeIdAndStatusNot(Integer showtimeId, String status);

    List<BookingSeat> findByBookingIdOrderByIdAsc(Integer bookingId);

    @Query("""
            SELECT bs.seat.id FROM BookingSeat bs
            WHERE bs.showtime.id = :showtimeId AND bs.status <> 'released'
            """)
    List<Integer> findTakenSeatIds(@Param("showtimeId") Integer showtimeId);

    long countByShowtimeIdAndStatusNot(Integer showtimeId, String status);

    long countByBookingId(Integer bookingId);
}
