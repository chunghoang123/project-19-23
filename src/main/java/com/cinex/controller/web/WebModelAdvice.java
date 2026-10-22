package com.cinex.controller.web;

import com.cinex.domain.User;
import com.cinex.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Cung cấp thông tin người dùng hiện tại cho mọi view Thymeleaf.
 */
@ControllerAdvice(basePackages = "com.cinex.controller.web")
@RequiredArgsConstructor
public class WebModelAdvice {

    private final CurrentUser currentUser;

    @ModelAttribute("currentUser")
    public User currentUser() {
        return currentUser.optional();
    }
}
