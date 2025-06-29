package com.example.tourify_system_be.service;

import com.example.tourify_system_be.dto.request.CreditCardRequest;
import com.example.tourify_system_be.dto.request.UserCreateRequest;
import com.example.tourify_system_be.dto.request.UserUpdateRequest;
import com.example.tourify_system_be.dto.response.CreditCardResponse;
import com.example.tourify_system_be.dto.response.UserResponse;
import com.example.tourify_system_be.entity.CreditCard;
import com.example.tourify_system_be.entity.TokenAuthentication;
import com.example.tourify_system_be.entity.User;
import com.example.tourify_system_be.exception.AppException;
import com.example.tourify_system_be.exception.ErrorCode;
import com.example.tourify_system_be.mapper.CreditCardMapper;
import com.example.tourify_system_be.mapper.UserMapper;
import com.example.tourify_system_be.repository.ICreditCardRepository;
import com.example.tourify_system_be.repository.ITokenAuthenticationRepository;
import com.example.tourify_system_be.repository.IUserRepository;
import com.example.tourify_system_be.security.CustomUserDetails;
import com.example.tourify_system_be.security.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    IUserRepository userRepository;
    ICreditCardRepository creditCardRepository;
    UserMapper userMapper;
    CreditCardMapper creditCardMapper;
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

    public UserResponse updateUser(String id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        userMapper.updateUser(user, request);
        user.setUpdatedAt(LocalDateTime.now());

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse getUserById(String id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    public Iterable<User> getAllUsers() {
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
            if(newPassword.equals(oldPassword)){
                return "New password same like old password";
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
                    token);

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

        if (firstName != null) {
            user.setFirstName(firstName);
        }
        if (lastName != null) {
            user.setLastName(lastName);
        }
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
        boolean phoneUsedByAnotherUser = userRepository.existsByPhoneNumber(phone)
                || phone.equalsIgnoreCase(user.getPhoneNumber());
        if (phoneUsedByAnotherUser) {
            throw new AppException(ErrorCode.PHONE_ALREADY_USED);
        } else {
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

    public User findOrCreateGoogleUser(String email, String name) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail(email);
                    user.setUserName(email.split("@")[0]);
                    user.setFirstName(name);
                    user.setRole("user");
                    user.setStatus("ACTIVE");
                    user.setPassword(""); // hoặc chuỗi bất kỳ vì không dùng
                    return userRepository.save(user);
                });
    }

    public List<CreditCardResponse> getAllCreditCardByToken(String bearerToken) {
        // Tách chuỗi "Bearer ..."
        String jwt = bearerToken.replace("Bearer ", "");
        String userId = jwtUtil.extractUserId(jwt);

//        List<CreditCard> creditCards = creditCardRepository.findAllByUser();
        List<CreditCard> creditCards = creditCardRepository.findAllByUser_UserId(userId);
        return creditCards.stream().map(creditCardMapper::toCreditCardResponse).toList();
    }

    //    @PostMapping("/creditcard")
//    public APIResponse<?> addCreditCard(@RequestHeader("Authorization") String token, @Valid @RequestBody AddCreditCardRequest request) {
//        return APIResponse.<CreditCardResponse>builder()
//                .result(userService.addCreditCard(token, request))
//                .build();
//    }
    public CreditCardResponse addCreditCard(String token, CreditCardRequest request) {
        String jwt = token.replace("Bearer ", "");
        String userId = jwtUtil.extractUserId(jwt);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
//        System.out.println(user);
//        CreditCard creditCard = creditCardMapper.toCreditCard(request);
        CreditCard creditCard = creditCardMapper.toCreditCard(request);
        creditCard.setCreatedAt(LocalDateTime.now());
        creditCard.setUpdatedAt(LocalDateTime.now());
        creditCard.setUser(user);
//        System.out.println(creditCard);
        creditCardRepository.save(creditCard);
//        System.out.println(creditCard);
        return creditCardMapper.toCreditCardResponse(creditCard);
    }
    /**
     * Khoá tài khoản (chỉ ADMIN có quyền khóa, và chỉ khóa được tài khoản của USER hoặc SUB_COMPANY).
     */
    @Transactional
    public void lockAccount(String bearerToken, String userId) {
        // 1) Xác thực token và chỉ cho ADMIN
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            throw new AppException(ErrorCode.OPERATION_NOT_ALLOWED);
        }
        String jwt = bearerToken.substring("Bearer ".length()).trim();
        jwtUtil.validateToken(jwt);
        CustomUserDetails currentUser = jwtUtil.getUserDetailsFromToken(jwt);
        if (currentUser == null ||
                ! "ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new AppException(ErrorCode.OPERATION_NOT_ALLOWED);
        }

        // 2) Lấy user cần khóa
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 3) Chỉ khóa được các account có role USER hoặc SUB_COMPANY
        String targetRole = Optional.ofNullable(u.getRole())
                .map(String::trim)
                .orElse("");
        boolean canBeLocked = "USER".equalsIgnoreCase(targetRole)
                || "SUB_COMPANY".equalsIgnoreCase(targetRole);
        if (!canBeLocked) {
            throw new AppException(ErrorCode.OPERATION_NOT_ALLOWED);
        }

        // 4) Nếu chưa ở trạng thái LOCKED, thì cập nhật
        if (!"LOCKED".equalsIgnoreCase(u.getStatus())) {
            u.setStatus("LOCKED");
            u.setUpdatedAt(LocalDateTime.now());
            userRepository.save(u);
        }
    }


    /**
     * Mở khoá tài khoản (chỉ ADMIN có quyền và chỉ cho phép mở khóa tài khoản USER hoặc SUB_COMPANY).
     */
    @Transactional
    public void unlockAccount(String bearerToken, String userId) {
        // 1) Xác thực token và chỉ cho ADMIN
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            throw new AppException(ErrorCode.OPERATION_NOT_ALLOWED);
        }
        String jwt = bearerToken.substring("Bearer ".length()).trim();
        jwtUtil.validateToken(jwt);
        CustomUserDetails currentUser = jwtUtil.getUserDetailsFromToken(jwt);
        if (currentUser == null ||
                !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new AppException(ErrorCode.OPERATION_NOT_ALLOWED);
        }

        // 2) Lấy user cần mở khóa
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 3) Chỉ mở khóa các account có role USER hoặc SUB_COMPANY
        String targetRole = Optional.ofNullable(u.getRole())
                .map(String::trim)
                .orElse("");
        boolean canBeUnlocked = "USER".equalsIgnoreCase(targetRole)
                || "SUB_COMPANY".equalsIgnoreCase(targetRole);
        if (!canBeUnlocked) {
            throw new AppException(ErrorCode.OPERATION_NOT_ALLOWED);
        }

        // 4) Nếu chưa ở trạng thái ACTIVE thì cập nhật
        if (!"ACTIVE".equalsIgnoreCase(u.getStatus())) {
            u.setStatus("ACTIVE");
            u.setUpdatedAt(LocalDateTime.now());
            userRepository.save(u);
        }
    }
}