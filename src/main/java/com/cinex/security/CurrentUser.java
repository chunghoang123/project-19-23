package com.cinex.security;

import com.cinex.domain.User;
import com.cinex.exception.NotFoundException;
import com.cinex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Tiện ích lấy người dùng đang đăng nhập từ SecurityContext.
 */
@Component
@RequiredArgsConstructor
public class CurrentUser {

    private final UserRepository userRepository;

    /** Người dùng hiện tại, hoặc {@code null} nếu khách chưa đăng nhập. */
    public User optional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails details)) {
            return null;
        }
        return userRepository.findById(details.getId()).orElse(null);
    }

    /** Người dùng hiện tại, ném lỗi nếu chưa đăng nhập. */
    public User require() {
        User user = optional();
        if (user == null) {
            throw new NotFoundException("Vui lòng đăng nhập để tiếp tục");
        }
        return user;
    }
}
