package com.cinex.repository;

import com.cinex.domain.Province;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProvinceRepository extends JpaRepository<Province, Integer> {

    Optional<Province> findByNameIgnoreCase(String name);

    Optional<Province> findByCode(String code);
}
