package com.cinex.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;

/**
 * Xử lý lỗi cho các controller web trong {@code com.cinex.controller.web}:
 * hiển thị thông báo thân thiện thay vì trang lỗi kỹ thuật.
 *
 * <p>Sau khi báo lỗi, người dùng được đưa về lại trang họ đang ở (theo header
 * {@code Referer}) để không mất ngữ cảnh, ví dụ lỗi khi bấm "Xác nhận thanh toán"
 * sẽ quay lại đúng trang chi tiết đơn thay vì trang chủ.</p>
 */
@Slf4j
@ControllerAdvice(basePackages = "com.cinex.controller.web")
public class WebExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public String handleNotFound(NotFoundException ex, RedirectAttributes redirectAttributes,
                                 HttpServletRequest request) {
        log.warn("Không tìm thấy dữ liệu: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:" + safeTarget(request);
    }

    @ExceptionHandler(BadRequestException.class)
    public String handleBadRequest(BadRequestException ex, RedirectAttributes redirectAttributes,
                                   HttpServletRequest request) {
        log.warn("Yêu cầu không hợp lệ: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:" + safeTarget(request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleForbidden(AccessDeniedException ex, RedirectAttributes redirectAttributes,
                                  HttpServletRequest request) {
        log.warn("Không đủ quyền truy cập: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thực hiện thao tác này");
        return "redirect:" + safeTarget(request);
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception ex, RedirectAttributes redirectAttributes,
                                   HttpServletRequest request) {
        log.error("Lỗi không mong đợi khi xử lý web request", ex);
        redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi, vui lòng thử lại");
        return "redirect:" + safeTarget(request);
    }

    /**
     * Trang đích sau khi báo lỗi: lấy từ header {@code Referer} nếu là đường dẫn
     * nội bộ của ứng dụng, ngược lại quay về trang chủ.
     */
    private String safeTarget(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            return "/";
        }
        try {
            URI uri = URI.create(referer);
            boolean sameHost = uri.getHost() == null
                    || uri.getHost().equalsIgnoreCase(request.getServerName());
            String path = uri.getPath();
            if (sameHost && path != null && !path.isBlank() && !path.startsWith("/login")) {
                return uri.getQuery() == null ? path : path + "?" + uri.getQuery();
            }
        } catch (IllegalArgumentException ex) {
            log.debug("Referer không hợp lệ: {}", referer);
        }
        return "/";
    }
}
