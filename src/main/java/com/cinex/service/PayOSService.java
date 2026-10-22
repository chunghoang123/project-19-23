package com.cinex.service;

import com.cinex.config.IntegrationProperties;
import com.cinex.domain.Booking;
import com.cinex.domain.Payment;
import com.cinex.domain.User;
import com.cinex.exception.BadRequestException;
import com.cinex.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.exception.PayOSException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.WebhookData;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Tích hợp cổng thanh toán PayOS: tạo link thanh toán và xác thực webhook.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayOSService {

    /** Mã đơn hàng gửi lên PayOS, dùng để đối soát khi webhook báo kết quả. */
    public record PaymentLink(long orderCode, String paymentLinkId, String checkoutUrl, String qrCode) {
    }

    private final Optional<PayOS> client;
    private final PaymentRepository paymentRepository;
    private final IntegrationProperties properties;
    private final CodeGenerator codeGenerator;

    public boolean isEnabled() {
        return client.isPresent();
    }

    /**
     * Tạo link thanh toán cho đơn đặt vé.
     *
     * @param booking đơn đang chờ thanh toán
     * @param user    người mua
     */
    @Transactional
    public PaymentLink createPaymentLink(Booking booking, User user, String returnUrl, String cancelUrl) {
        PayOS payOS = client.orElseThrow(
                () -> new BadRequestException("Chưa cấu hình cổng thanh toán PayOS"));

        // Tạo bản ghi thanh toán trước để lấy mã đơn làm orderCode của PayOS
        Payment payment = new Payment();
        payment.setCode(codeGenerator.generate("PAY"));
        payment.setBooking(booking);
        payment.setUser(user);
        payment.setProvider("payos");
        payment.setAmount(booking.getFinalAmount());
        payment.setStatus(Payment.STATUS_PENDING);
        payment = paymentRepository.save(payment);

        long orderCode = payment.getId();
        // PayOS chỉ nhận mô tả tối đa 25 ký tự
        String description = ("CineX " + booking.getCode());
        if (description.length() > 25) {
            description = description.substring(0, 25);
        }
        CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(booking.getFinalAmount().longValue())
                .description(description)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .buyerName(user.getName())
                .buyerEmail(user.getEmail())
                .buyerPhone(user.getPhone())
                .build();

        try {
            CreatePaymentLinkResponse response = payOS.paymentRequests().create(request);
            payment.setProviderOrderId(String.valueOf(orderCode));
            payment.setProviderRef(response.getPaymentLinkId());
            payment.setCheckoutUrl(response.getCheckoutUrl());
            paymentRepository.save(payment);

            return new PaymentLink(orderCode, response.getPaymentLinkId(),
                    response.getCheckoutUrl(), response.getQrCode());
        } catch (PayOSException ex) {
            log.error("PayOS không tạo được link thanh toán cho đơn {}", booking.getCode(), ex);
            payment.setStatus(Payment.STATUS_FAILED);
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);
            throw new BadRequestException("Không tạo được link thanh toán: " + ex.getMessage());
        }
    }

    /**
     * Xác thực chữ ký webhook do PayOS gửi lại.
     */
    public WebhookData verifyWebhook(Object payload) {
        PayOS payOS = client.orElseThrow(
                () -> new BadRequestException("Chưa cấu hình cổng thanh toán PayOS"));
        try {
            return payOS.webhooks().verify(payload);
        } catch (PayOSException ex) {
            log.warn("Webhook PayOS không hợp lệ: {}", ex.getMessage());
            throw new BadRequestException("Dữ liệu webhook không hợp lệ");
        }
    }

    /** Tìm bản ghi thanh toán theo mã đơn PayOS gửi lên. */
    @Transactional(readOnly = true)
    public Optional<Payment> findByOrderCode(long orderCode) {
        return paymentRepository.findByProviderOrderId(String.valueOf(orderCode));
    }

    public String baseUrl() {
        return properties.getPayosBaseUrl();
    }
}
