package com.cinex.controller.web;

import com.cinex.domain.User;
import com.cinex.repository.UserRepository;
import com.cinex.security.CurrentUser;
import com.cinex.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final CurrentUser currentUser;
    private final AuthService authService;
    private final UserRepository userRepository;

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("user", currentUser.require());
        return "profile";
    }

    @PostMapping("/profile")
    public String update(@RequestParam String name,
                         @RequestParam(required = false) String phone,
                         RedirectAttributes redirectAttributes) {
        User user = currentUser.require();
        user.setName(name.trim());
        if (phone != null && !phone.isBlank()) {
            userRepository.findByPhone(phone.trim()).ifPresent(other -> {
                if (!other.getId().equals(user.getId())) {
                    throw new com.cinex.exception.BadRequestException(
                            "Số điện thoại đã được tài khoản khác sử dụng");
                }
            });
            user.setPhone(phone.trim());
        }
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Cập nhật hồ sơ thành công");
        return "redirect:/profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 RedirectAttributes redirectAttributes) {
        User user = currentUser.require();
        authService.changePassword(user, oldPassword, newPassword);
        redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công");
        return "redirect:/profile";
    }
}
