package com.cinex.repository;

import com.cinex.domain.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    Optional<Booking> findByCode(String code);

    List<Booking> findTop10ByOrderByCreatedAtDesc();

    List<Booking> findByUserIdOrderByCreatedAtDesc(Integer userId);

    /** Các đơn đang chờ thanh toán đã tạo trước thời điểm {@code before}. */
    List<Booking> findByStatusAndCreatedAtBefore(String status, LocalDateTime before);

    Page<Booking> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.createdAt >= :from AND b.createdAt < :to AND b.status <> 'cancelled'
            ORDER BY b.createdAt ASC
            """)
    List<Booking> findInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT COALESCE(SUM(b.finalAmount), 0) FROM Booking b
            WHERE b.status = 'confirmed' AND b.createdAt >= :from AND b.createdAt < :to
            """)
    long sumRevenueInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    long countByStatus(String status);

    long countByCreatedAtAfter(LocalDateTime after);
}
