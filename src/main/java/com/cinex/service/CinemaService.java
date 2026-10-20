package com.cinex.service;

import com.cinex.domain.Cinema;
import com.cinex.exception.NotFoundException;
import com.cinex.repository.CinemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CinemaService {

    private final CinemaRepository cinemaRepository;

    @Transactional(readOnly = true)
    public List<Cinema> findAllActive() {
        return cinemaRepository.findByStatusOrderByNameAsc("active");
    }

    @Transactional(readOnly = true)
    public List<Cinema> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAllActive();
        }
        return cinemaRepository.findByNameContainingIgnoreCaseOrderByNameAsc(keyword.trim());
    }

    @Transactional(readOnly = true)
    public Cinema getById(Integer id) {
        return cinemaRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("rạp", id));
    }

    /** Danh sách rạp nhóm theo tỉnh/thành để hiển thị trang "Rạp chiếu phim". */
    @Transactional(readOnly = true)
    public Map<String, List<Cinema>> groupByProvince() {
        Map<String, List<Cinema>> grouped = new LinkedHashMap<>();
        for (Cinema cinema : findAllActive()) {
            String province = cinema.getProvince() != null
                    ? cinema.getProvince().getName()
                    : "Khác";
            grouped.computeIfAbsent(province, key -> new java.util.ArrayList<>()).add(cinema);
        }
        return grouped;
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return cinemaRepository.countByStatus("active");
    }
}
