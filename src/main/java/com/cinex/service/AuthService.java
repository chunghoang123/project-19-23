package com.cinex.service;

import com.cinex.domain.Role;
import com.cinex.domain.User;
import com.cinex.dto.RegisterForm;
import com.cinex.exception.BadRequestException;
import com.cinex.repository.RoleRepository;
import com.cinex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterForm form) {
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
        String phone = form.getPhone().trim();

        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không khớp");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("Email đã được sử dụng");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new BadRequestException("Số điện thoại đã được sử dụng");
        }

        Role role = roleRepository.findByName("user")
                .orElseThrow(() -> new BadRequestException("Hệ thống chưa cấu hình vai trò người dùng"));

        User user = new User();
        user.setName(form.getName().trim());
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRole(role);
        user.setStatus("active");

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email).orElse(null);
    }

    @Transactional
    public void changePassword(User user, String oldPassword, String newPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Mật khẩu cũ không đúng");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BadRequestException("Mật khẩu mới tối thiểu 6 ký tự");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
