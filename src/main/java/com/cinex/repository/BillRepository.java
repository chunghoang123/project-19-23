package com.cinex.repository;

import com.cinex.domain.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Integer> {

    Optional<Bill> findByCode(String code);

    Optional<Bill> findByBookingId(Integer bookingId);

    List<Bill> findByUserIdOrderByIssuedAtDesc(Integer userId);
}
