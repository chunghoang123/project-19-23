package com.cinex.repository;

import com.cinex.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    Optional<Payment> findByCode(String code);

    List<Payment> findByBookingIdOrderByIdDesc(Integer bookingId);

    List<Payment> findByUserIdOrderByIdDesc(Integer userId);

    /** Bản ghi thanh toán đang chờ của một đơn (dùng cho thanh toán online). */
    Optional<Payment> findByBookingIdAndStatus(Integer bookingId, String status);

    /** Tra cứu giao dịch theo mã đơn gửi lên cổng thanh toán. */
    Optional<Payment> findByProviderOrderId(String providerOrderId);
}
