package com.cinex.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SeatTest {

    @Test
    @DisplayName("Nhãn ghế ghép từ hàng và số ghế")
    void seatLabel() {
        Seat seat = new Seat();
        seat.setRowLabel("A");
        seat.setSeatNumber(5);
        assertEquals("A5", seat.getLabel());
    }

    @Test
    @DisplayName("Ghế mặc định là ghế thường và đang hoạt động")
    void defaults() {
        Seat seat = new Seat();
        assertEquals(Seat.TYPE_STANDARD, seat.getSeatType());
        assertEquals(Boolean.TRUE, seat.getIsActive());
    }
}
