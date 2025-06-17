package com.example.tourify_system_be.service;

import com.example.tourify_system_be.dto.request.UserCreateRequest;
import com.example.tourify_system_be.dto.request.UserUpdateRequest;
import com.example.tourify_system_be.dto.response.UserResponse;
import com.example.tourify_system_be.entity.TokenAuthentication;
import com.example.tourify_system_be.entity.User;
import com.example.tourify_system_be.exception.AppException;
import com.example.tourify_system_be.exception.ErrorCode;
import com.example.tourify_system_be.mapper.UserMapper;
import com.example.tourify_system_be.repository.ITokenAuthenticationRepository;
import com.example.tourify_system_be.repository.IUserRepository;
import com.example.tourify_system_be.security.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    IUserRepository userRepository;
    UserMapper userMapper;
    EmailService emailService;
    PasswordEncoder passwordEncoder;
    JwtUtil jwtUtil;
    ITokenAuthenticationRepository tokenRepo;

    Pattern EMAIL_REGEX = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    Duration TOKEN_VALID_DURATION = Duration.ofHours(24);


    Map<String, PendingUser> pendingUsers = Collections.synchronizedMap(new HashMap<>());

    static class PendingUser {
        private final User user;
        private final Instant tokenCreatedAt;

        public PendingUser(User user, Instant tokenCreatedAt) {
            this.user = user;
            this.tokenCreatedAt = tokenCreatedAt;
        }

        public User getUser() {
            return user;
        }

        public Instant getTokenCreatedAt() {
            return tokenCreatedAt;
        }
    }

    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    public UserResponse updateUser(String id, UserUpdateRequest request){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        userMapper.updateUser(user, request);
        user.setUpdatedAt(LocalDateTime.now());

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse getUserById(String id){
        return userMapper.toUserResponse(userRepository.findById(id).orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    public Iterable<User> getAllUsers(){
        System.out.println(userRepository.findAll());
        return userRepository.findAll();
    }

    public boolean updateAvatar(String userName, String avatarUrl) {
        return userRepository.findByUserName(userName).map(user -> {
            user.setAvatar(avatarUrl);
            userRepository.save(user);
            return true;
        }).orElse(false);
    }

    public String changePassword(String username, String oldPassword, String newPassword, String confirmPassword) {
        if (newPassword.length() < 6) {
            return "New password must be at least 6 characters long";
        }
        return userRepository.findByUserName(username).map(user -> {
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return "Old password incorrect";
            }
            if (!newPassword.equals(confirmPassword)) {
                return "New password confirmation does not match";
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return "Password changed successfully";
        }).orElse("Username does not exist");
    }

    /**
     * Gửi email xác nhận đăng ký và lưu user tạm thời vào bộ nhớ.
     * Không lưu vào DB cho đến khi xác nhận thành công.
     */
    public boolean sendRegistrationToken(UserCreateRequest dto) {
        if (dto == null || dto.getEmail() == null || !EMAIL_REGEX.matcher(dto.getEmail()).matches()) {
            return false;
        }

        if (dto.getPasswordConfirm() == null || !dto.getPasswordConfirm().equals(dto.getPassword())) {
            return false;
        }

        // Kiểm tra email hoặc username đã tồn tại
        if (userRepository.existsByEmail(dto.getEmail()) || userRepository.existsByUserName(dto.getUserName())) {
            return false;
        }

        try {
            User user = User.builder()
                    .userName(dto.getUserName())
                    .firstName(dto.getFirstName())
                    .lastName(dto.getLastName())
                    .email(dto.getEmail())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .gender(Boolean.TRUE.equals(dto.getGender()))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .status("pending")
                    .build();

            String token = UUID.randomUUID().toString();
            pendingUsers.put(token, new PendingUser(user, Instant.now()));

            emailService.sendRegistrationConfirmationEmail(
                    user.getEmail(),
                    user.getFirstName() != null ? user.getFirstName() : user.getUserName(),
                    token
            );

            return true;
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email xác nhận: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xác nhận token và lưu user vào DB với status = "active".
     */

    public String confirmTokenAndCreateUser(String token) {
        PendingUser pendingUser = pendingUsers.get(token);
        if (pendingUser == null) {
            System.err.println("Token không hợp lệ hoặc đã được sử dụng: " + token);
            return null;
        }

        // Kiểm tra hạn sử dụng token
        if (pendingUser.getTokenCreatedAt().plus(TOKEN_VALID_DURATION).isBefore(Instant.now())) {
            pendingUsers.remove(token);
            System.err.println("Token hết hạn: " + token);
            return null;
        }

        User user = pendingUser.getUser();

        // Kiểm tra lại trong DB trước khi lưu
        if (userRepository.existsByEmail(user.getEmail()) || userRepository.existsByUserName(user.getUserName())) {
            pendingUsers.remove(token);
            System.err.println("Email hoặc username đã tồn tại: " + user.getEmail());
            return null;
        }

        try {
            user.setStatus("active");
            user.setRole("user");
            ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
            LocalDateTime now = LocalDateTime.now(vietnamZone);
            user.setCreatedAt(now);
            user.setUpdatedAt(now);

            userRepository.save(user);

            // tao token cho user luu cookie
            String accessToken = jwtUtil.generateToken( // ✅ gọi đúng instance
                    user.getUserId(), user.getUserName(), user.getRole());

            LocalDateTime expiresAt = now.plusDays(1);

            TokenAuthentication tokenEntity = TokenAuthentication.builder()
                    .tokenValue(token)
                    .createAt(now)
                    .expiresAt(expiresAt)
                    .isUsed(true)
                    .user(user)
                    .build();
            tokenRepo.save(tokenEntity);

            pendingUsers.remove(token);

            return accessToken;
        } catch (Exception e) {
            System.err.println("Lỗi khi lưu user vào DB: " + e.getMessage());
            return null;
        }
    }

    public void updateName(String username, String firstName, String lastName) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public void updateEmail(String username, String email) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Kiểm tra email đã được dùng bởi người khác chưa
        boolean emailUsedByAnotherUser = userRepository.existsByEmail(email) &&
                !email.equalsIgnoreCase(user.getEmail());

        if (emailUsedByAnotherUser) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_USED);
        }

        user.setUpdatedAt(LocalDateTime.now());
        user.setEmail(email);
        userRepository.save(user);
    }

    public void updatePhone(String username, String phone) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        // Kiểm tra phonenumber đã được dùng bởi người khác chưa
        boolean phoneUsedByAnotherUser = userRepository.existsByPhoneNumber(phone) || phone.equalsIgnoreCase(user.getPhoneNumber());
        if (phoneUsedByAnotherUser) {
            throw new AppException(ErrorCode.PHONE_ALREADY_USED);
        }else{
            user.setPhoneNumber(phone);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    public void updateAddress(String username, String address) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setAddress(address);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public void updateDob(String username, String dob) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        LocalDate parsedDate = LocalDate.parse(dob, DateTimeFormatter.ISO_LOCAL_DATE);

        user.setDob(parsedDate.atStartOfDay());

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }



    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean userNameExists(String username) {
        return userRepository.existsByUserName(username);
    }

    public void updateAccountStatus(String userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
