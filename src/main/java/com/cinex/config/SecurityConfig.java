package com.cinex.config;

import com.cinex.security.CinexUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CinexUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/", "/movies", "/movies/**", "/cinemas", "/cinemas/**",
                        "/showtimes", "/promotions",
                        "/css/**", "/js/**", "/images/**", "/favicon.ico",
                        "/api/health", "/api/public/**", "/api/payments/payos/webhook",
                        "/login", "/register", "/logout"
                ).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/admin-api/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(login -> login
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .userDetailsService(userDetailsService)
            .csrf(csrf -> csrf
                    // Webhook đến từ PayOS không gửi kèm CSRF token
                    .ignoringRequestMatchers("/api/payments/payos/webhook"))
            .exceptionHandling(ex -> {
                ex.accessDeniedPage("/login?denied");
                // REST API trả JSON 401, còn trang web thì chuyển hướng sang trang đăng nhập.
                // Phải đăng ký cả hai: nếu chỉ có một mapping thì Spring dùng nó cho mọi request.
                ex.defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), apiMatcher());
                ex.defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"), AnyRequestMatcher.INSTANCE);
                ex.defaultAccessDeniedHandlerFor((request, response, denied) -> {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                            "{\"success\":false,\"message\":\"Bạn không có quyền thực hiện thao tác này\"}");
                }, apiMatcher());
            });

        return http.build();
    }

    /** Các endpoint REST trả về JSON thay vì chuyển hướng sang trang đăng nhập. */
    private PathPatternRequestMatcher apiMatcher() {
        return PathPatternRequestMatcher.withDefaults().matcher("/api/**");
    }
}
