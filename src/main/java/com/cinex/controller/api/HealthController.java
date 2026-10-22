package com.cinex.controller.api;

import com.cinex.config.IntegrationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * API công khai: kiểm tra tình trạng hệ thống.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HealthController {

    private final IntegrationProperties integrationProperties;

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("message", "CineX API is running");
        body.put("service", "cinex-spring-boot");
        body.put("timestamp", LocalDateTime.now().toString());
        return body;
    }

    /**
     * Các tích hợp bên ngoài đã được cấu hình (chỉ trả về tên, không lộ khoá).
     */
    @GetMapping("/public/integrations")
    public Map<String, Object> integrations() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("configured", integrationProperties.configured());
        body.put("pending", java.util.List.of(
                "TMDB", "YouTube", "PayOS", "NVIDIA AI", "Google OAuth", "Firebase").stream()
                .filter(name -> !integrationProperties.configured().contains(name))
                .toList());
        return body;
    }
}
