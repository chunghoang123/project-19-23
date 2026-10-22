package com.cinex.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cấu hình cho các tích hợp bên ngoài.
 *
 * <p>Khung (skeleton) hiện tại chưa gọi ra dịch vụ nào, tất cả khoá đều để trống
 * và được đọc từ biến môi trường. Khi bổ sung khoá thật chỉ cần điền vào file
 * {@code .env} (không commit) hoặc khai báo biến môi trường của Docker Compose.</p>
 *
 * <p>Các trường dùng cho: đồng bộ phim TMDB, trailer YouTube, thanh toán PayOS,
 * trợ lý AI NVIDIA, đăng nhập Google/Firebase.</p>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.integration")
public class IntegrationProperties {

    private String tmdbApiKey;
    private String tmdbApiUrl = "https://api.themoviedb.org/3";
    private String tmdbLanguage = "vi-VN";
    private String tmdbRegion = "VN";

    private String youtubeApiKey;

    private String payosClientId;
    private String payosApiKey;
    private String payosChecksumKey;
    private String payosBaseUrl = "https://api-merchant.payos.vn";

    private String nvidiaApiKey;
    private String nvidiaModel = "meta/llama-3.1-8b-instruct";
    private String nvidiaApiUrl = "https://integrate.api.nvidia.com/v1/chat/completions";

    private String googleClientId;

    private String firebaseProjectId;
    private String firebaseClientEmail;
    private String firebasePrivateKey;

    /** Tên các tích hợp đã được cấu hình, dùng cho trang trạng thái hệ thống. */
    public java.util.List<String> configured() {
        java.util.List<String> result = new java.util.ArrayList<>();
        if (hasText(tmdbApiKey)) {
            result.add("TMDB");
        }
        if (hasText(youtubeApiKey)) {
            result.add("YouTube");
        }
        if (hasText(payosClientId) && hasText(payosApiKey) && hasText(payosChecksumKey)) {
            result.add("PayOS");
        }
        if (hasText(nvidiaApiKey)) {
            result.add("NVIDIA AI");
        }
        if (hasText(googleClientId)) {
            result.add("Google OAuth");
        }
        if (hasText(firebaseProjectId) && hasText(firebaseClientEmail) && hasText(firebasePrivateKey)) {
            result.add("Firebase");
        }
        return result;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /** Đủ cả 3 khoá PayOS thì mới tạo được link thanh toán. */
    public boolean isPayOSReady() {
        return hasText(payosClientId) && hasText(payosApiKey) && hasText(payosChecksumKey);
    }

    /** Đã cấu hình TMDB thì mới đồng bộ được phim. */
    public boolean isTmdbReady() {
        return hasText(tmdbApiKey);
    }
}
