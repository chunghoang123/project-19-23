package com.cinex.repository;

import com.cinex.domain.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenRepository extends JpaRepository<Screen, Integer> {

    List<Screen> findByCinemaIdOrderByNameAsc(Integer cinemaId);

    long countByCinemaId(Integer cinemaId);
}
