package com.cinex.repository;

import com.cinex.domain.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Integer> {

    List<Seat> findByScreenIdOrderByRowLabelAscSeatNumberAsc(Integer screenId);

    List<Seat> findByScreenIdAndIsActiveTrueOrderByRowLabelAscSeatNumberAsc(Integer screenId);

    void deleteByScreenId(Integer screenId);
}
