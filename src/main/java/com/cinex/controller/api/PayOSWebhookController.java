package com.cinex.controller.api;

import com.cinex.domain.Booking;
import com.cinex.domain.Payment;
import com.cinex.service.BookingService;
import com.cinex.service.PayOSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.payos.model.webhooks.WebhookData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Webhook nhận kết quả thanh toán từ PayOS.
 *
 * <p>PayOS gửi POST kèm chữ ký; dữ liệu được xác thực bằng checksum key
 * trước khi cập nhật đơn đặt vé.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/payments/payos")
@RequiredArgsConstructor
public class PayOSWebhookController {

    private final PayOSService payOSService;
    private final BookingService bookingService;

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> handleWebhook(@RequestBody Map<String, Object> payload) {
        WebhookData data = payOSService.verifyWebhook(payload);

        Map<String, Object> body = new LinkedHashMap<>();
        String code = data.getCode();
        boolean success = "00".equals(code);

        Optional<Payment> payment = payOSService.findByOrderCode(data.getOrderCode());
        if (payment.isEmpty()) {
            log.warn("Webhook PayOS không tìm thấy giao dịch với orderCode {}", data.getOrderCode());
            body.put("success", false);
            body.put("message", "Không tìm thấy giao dịch");
            return ResponseEntity.ok(body);
        }

        Booking booking = payment.get().getBooking();
        if (success) {
            bookingService.markAsPaid(booking.getCode(), String.valueOf(data.getOrderCode()),
                    data.getPaymentLinkId());
            log.info("Đã xác nhận thanh toán PayOS cho đơn {}", booking.getCode());
        } else {
            log.warn("Thanh toán PayOS thất bại cho đơn {}: {} - {}", booking.getCode(), code, data.getDesc());
        }

        body.put("success", success);
        body.put("bookingCode", booking.getCode());
        body.put("orderCode", data.getOrderCode());
        body.put("message", data.getDesc());
        return ResponseEntity.ok(body);
    }
}
