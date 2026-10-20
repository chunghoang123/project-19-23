package com.cinex.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Xử lý lỗi cho các controller web trong {@code com.cinex.controller.web}:
 * hiển thị thông báo thân thiện thay vì trang lỗi kỹ thuật.
 */
@Slf4j
@ControllerAdvice(basePackages = "com.cinex.controller.web")
public class WebExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public String handleNotFound(NotFoundException ex, RedirectAttributes redirectAttributes) {
        log.warn("Không tìm thấy dữ liệu: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/";
    }

    @ExceptionHandler(BadRequestException.class)
    public String handleBadRequest(BadRequestException ex, RedirectAttributes redirectAttributes) {
        log.warn("Yêu cầu không hợp lệ: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleForbidden(AccessDeniedException ex, RedirectAttributes redirectAttributes) {
        log.warn("Không đủ quyền truy cập: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thực hiện thao tác này");
        return "redirect:/";
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception ex, RedirectAttributes redirectAttributes) {
        log.error("Lỗi không mong đợi khi xử lý web request", ex);
        redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi, vui lòng thử lại");
        return "redirect:/";
    }
}
