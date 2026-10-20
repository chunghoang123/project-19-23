package com.cinex.controller.api;

import com.cinex.domain.Booking;
import com.cinex.domain.User;
import com.cinex.dto.CheckoutForm;
import com.cinex.dto.SeatMapView;
import com.cinex.security.CurrentUser;
import com.cinex.service.BookingService;
import com.cinex.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * API cho thao tác chọn và giữ ghế.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SeatApiController {

    private final SeatService seatService;
    private final BookingService bookingService;
    private final CurrentUser currentUser;

    /** Bản đồ ghế của một suất chiếu (công khai, để hiển thị sơ đồ ghế). */
    @GetMapping("/public/seat-map/{showtimeId}")
    public SeatMapView seatMap(@PathVariable Integer showtimeId) {
        return seatService.buildSeatMap(showtimeId);
    }

    /** Giữ ghế, trả về mã đơn để tiến tới bước thanh toán. */
    @PostMapping("/seat-holds")
    public ResponseEntity<Map<String, Object>> holdSeats(@Valid @RequestBody CheckoutForm form) {
        User user = currentUser.require();
        Booking booking = bookingService.holdSeats(user, form);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("bookingCode", booking.getCode());
        body.put("seatCount", form.getSeatIds().size());
        body.put("totalAmount", booking.getTotalAmount());
        body.put("holdExpireMinutes", 15);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    /** Hủy giữ ghế. */
    @DeleteMapping("/seat-holds/{code}")
    public Map<String, Object> releaseHold(@PathVariable String code) {
        User user = currentUser.require();
        bookingService.cancel(user, code);
        return Map.of("success", true, "message", "Đã trả lại ghế");
    }
}
