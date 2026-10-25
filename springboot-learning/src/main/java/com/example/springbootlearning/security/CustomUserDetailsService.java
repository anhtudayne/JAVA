package com.example.springbootlearning.security;

import com.example.springbootlearning.model.User;
import com.example.springbootlearning.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 📘 BÀI 18 — Custom UserDetailsService
 *
 * Cầu nối giữa Spring Security và Cơ sở dữ liệu:
 * - Khi client đăng nhập, Spring Security gọi loadUserByUsername(email)
 * - Tra cứu User từ UserRepository kèm danh sách Roles
 * - Chuyển đổi thành UserDetails để DaoAuthenticationProvider đối soát password hash
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + username));

        return new CustomUserDetails(user);
    }
}
