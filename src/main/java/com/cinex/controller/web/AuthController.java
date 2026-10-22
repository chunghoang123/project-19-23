package com.cinex.controller.web;

import com.cinex.domain.User;
import com.cinex.dto.RegisterForm;
import com.cinex.security.CurrentUser;
import com.cinex.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CurrentUser currentUser;

    @GetMapping("/login")
    public String loginPage(Model model,
                            @org.springframework.web.bind.annotation.RequestParam(required = false) String error,
                            @org.springframework.web.bind.annotation.RequestParam(required = false) String logout) {
        if (currentUser.optional() != null) {
            return "redirect:/";
        }
        model.addAttribute("error", error);
        model.addAttribute("logout", logout);
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (currentUser.optional() != null) {
            return "redirect:/";
        }
        model.addAttribute("registerForm", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        User user = authService.register(form);
        redirectAttributes.addFlashAttribute("success",
                "Đăng ký thành công. Chào mừng " + user.getName() + " đến với CineX!");
        return "redirect:/login";
    }
}
