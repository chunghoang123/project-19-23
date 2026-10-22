package com.cinex.repository;

import com.cinex.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    List<Ticket> findByBookingIdOrderByIdAsc(Integer bookingId);

    List<Ticket> findByUserIdOrderByIdDesc(Integer userId);

    Optional<Ticket> findByTicketCode(String ticketCode);
}
