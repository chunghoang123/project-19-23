package com.cinex.repository;

import com.cinex.domain.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CinemaRepository extends JpaRepository<Cinema, Integer> {

    List<Cinema> findByStatusOrderByNameAsc(String status);

    List<Cinema> findByNameContainingIgnoreCaseOrderByNameAsc(String keyword);

    List<Cinema> findByProvinceIdOrderByNameAsc(Integer provinceId);

    Optional<Cinema> findByName(String name);

    long countByStatus(String status);

    /** Rạp gần vị trí người dùng, sắp xếp theo khoảng cách đường thẳng (haversine). */
    @Query("""
            SELECT c FROM Cinema c
            WHERE c.status = 'active'
              AND c.lat IS NOT NULL AND c.lng IS NOT NULL
            ORDER BY (
                6371.0 * acos(
                    cos(radians(:lat)) * cos(radians(c.lat)) * cos(radians(c.lng) - radians(:lng))
                    + sin(radians(:lat)) * sin(radians(c.lat))
                )
            )
            """)
    List<Cinema> findNearby(@Param("lat") double lat, @Param("lng") double lng);
}
