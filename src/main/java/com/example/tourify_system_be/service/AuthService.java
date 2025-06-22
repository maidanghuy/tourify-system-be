package com.example.tourify_system_be.service;

import com.example.tourify_system_be.dto.request.APIResponse;
import com.example.tourify_system_be.dto.request.LoginRequest;
import com.example.tourify_system_be.dto.response.LoginResponse;
import com.example.tourify_system_be.dto.response.UserResponse;
import com.example.tourify_system_be.entity.TokenAuthentication;
import com.example.tourify_system_be.entity.User;
import com.example.tourify_system_be.exception.AppException;
import com.example.tourify_system_be.exception.ErrorCode;
import com.example.tourify_system_be.mapper.UserMapper;
import com.example.tourify_system_be.repository.ITokenAuthenticationRepository;
import com.example.tourify_system_be.repository.IUserRepository;
import com.example.tourify_system_be.security.JwtUtil;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class AuthService {
    IUserRepository userRepository;
    ITokenAuthenticationRepository tokenRepo;
    JavaMailSender mailSender;
    PasswordEncoder passwordEncoder;
    JwtUtil jwtUtil;
    UserMapper userMapper;

    public void sendResetPasswordEmail(String email) {
//        System.out.println(email);
        User user = userRepository.findAll().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND));

//        System.out.println(user);
        String token = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = now.plusMinutes(30); // expires in 0.5 hour

        TokenAuthentication tokenAuth = new TokenAuthentication();
        tokenAuth.setTokenValue(token);
        tokenAuth.setCreateAt(now);
        tokenAuth.setExpiresAt(expiration);
        tokenAuth.setIsUsed(false);
        tokenAuth.setUser(user);

        tokenRepo.save(tokenAuth);

        String resetLink = "http://localhost:8080/tourify/reset_password?token=" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setTo(email);
            helper.setSubject("Reset your password");
            helper.setText("Click the link to reset your password: " + resetLink);
            mailSender.send(message);
        } catch (Exception e) {
            throw new AppException(ErrorCode.FAIL_TO_SEEN_EMAIL);
        }
    }

    public void resetPassword(String token, String newPassword, String confirmPassword) {
        TokenAuthentication tokenAuth = tokenRepo.findByTokenValue(token);
        if (tokenAuth == null) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        if (tokenAuth.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        if (Boolean.TRUE.equals(tokenAuth.getIsUsed())) {
            throw new AppException(ErrorCode.TOKEN_ALREADY_USED);
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new AppException(ErrorCode.PASSWORD_CONFIRMATION_MISMATCH);
        }

        User user = tokenAuth.getUser();
        user.setUpdatedAt(LocalDateTime.now());

        // Hash password
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);

        userRepository.save(user);

        tokenAuth.setIsUsed(true);
        tokenRepo.save(tokenAuth);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        return userRepository.findByUserName(request.getUsername())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .map(user -> {
                    String token = jwtUtil.generateToken( // ✅ gọi đúng instance
                            user.getUserId(), user.getUserName(), user.getRole());

                    ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
                    LocalDateTime now = LocalDateTime.now(vietnamZone);
                    LocalDateTime expiresAt = now.plusDays(1);

                    TokenAuthentication tokenEntity = TokenAuthentication.builder()
                            .tokenValue(token)
                            .createAt(now)
                            .expiresAt(expiresAt)
                            .isUsed(true)
                            .user(user)
                            .build();

                    tokenRepo.save(tokenEntity);

                    return new LoginResponse("Login successful!", token);
                })
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));
    }

    public boolean logout(String tokenValue) {
        TokenAuthentication token = tokenRepo.findByTokenValue(tokenValue);
        if (token != null) {
            token.setIsUsed(false);  // hoặc xóa tokenRepo.delete(token);
            tokenRepo.save(token);
            return true;
        }
        return false;
    }

    public UserResponse getUserFromToken(String token) {
        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(user);
    }

    @Transactional
    public APIResponse<Map<String, Object>> loginWithGoogle(OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUserName(email.split("@")[0]);
                    newUser.setFirstName(name);
                    newUser.setRole("user");
                    newUser.setStatus("ACTIVE");
                    newUser.setPassword("");
                    newUser.setCreatedAt(LocalDateTime.now());
                    newUser.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(newUser);
                });

        String token = jwtUtil.generateToken(user.getUserId(), user.getUserName(), user.getRole());

        TokenAuthentication tokenEntity = TokenAuthentication.builder()
                .tokenValue(token)
                .createAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(1))
                .isUsed(true)
                .user(user)
                .build();

        tokenRepo.save(tokenEntity);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "Login by Gmail successful");
        result.put("token", token);
        result.put("userName", user.getUserName()); // 👈 DỮ LIỆU MỚI

        return APIResponse.<Map<String, Object>>builder()
                .code(1000)
                .message("Login by Gmail successful")
                .result(result)
                .build();
    }

}
