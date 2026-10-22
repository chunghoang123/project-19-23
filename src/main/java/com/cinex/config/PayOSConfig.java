package com.cinex.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;
import vn.payos.core.ClientOptions;

/**
 * Khởi tạo client PayOS khi đã cấu hình đủ 3 khoá.
 *
 * <p>Nếu thiếu khoá thì không tạo bean, các chức năng thanh toán online
 * sẽ tự báo lỗi "chưa cấu hình" thay vì làm app không khởi động được.</p>
 */
@Configuration
public class PayOSConfig {

    @Bean
    @ConditionalOnExpression("!'${app.integration.payos-client-id:}'.isEmpty()"
            + " && !'${app.integration.payos-api-key:}'.isEmpty()"
            + " && !'${app.integration.payos-checksum-key:}'.isEmpty()")
    public PayOS payOSClient(IntegrationProperties properties) {
        return new PayOS(ClientOptions.builder()
                .clientId(properties.getPayosClientId())
                .apiKey(properties.getPayosApiKey())
                .checksumKey(properties.getPayosChecksumKey())
                .baseURL(properties.getPayosBaseUrl())
                .build());
    }
}
