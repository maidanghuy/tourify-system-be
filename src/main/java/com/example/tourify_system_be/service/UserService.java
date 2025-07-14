package com.example.tourify_system_be.service;

import com.example.tourify_system_be.dto.request.CreditCardRequest;
import com.example.tourify_system_be.dto.request.UserCreateRequest;
import com.example.tourify_system_be.dto.request.UserUpdateRequest;
import com.example.tourify_system_be.dto.response.BookingTourResponse;
import com.example.tourify_system_be.dto.response.CreditCardResponse;
import com.example.tourify_system_be.dto.response.TourResponse;
import com.example.tourify_system_be.dto.response.UserResponse;
import com.example.tourify_system_be.entity.*;
import com.example.tourify_system_be.exception.AppException;
import com.example.tourify_system_be.exception.ErrorCode;
import com.example.tourify_system_be.mapper.CreditCardMapper;
import com.example.tourify_system_be.mapper.TourBookingMapper;
import com.example.tourify_system_be.mapper.TourMapper;
import com.example.tourify_system_be.mapper.UserMapper;
import com.example.tourify_system_be.repository.*;
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
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.tourify_system_be.exception.ErrorCode.OPERATION_NOT_ALLOWED;
import static com.example.tourify_system_be.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    IUserRepository userRepository;
    ICreditCardRepository creditCardRepository;
    ITourFavoriteRepository tourFavoriteRepository;
    ITourRepository tourRepository;
    IBookingTourRepository bookingTourRepository;
    UserMapper userMapper;
    TourBookingMapper tourBookingMapper;
    CreditCardMapper creditCardMapper;
    EmailService emailService;
    PasswordEncoder passwordEncoder;
    JwtUtil jwtUtil;
    ITokenAuthenticationRepository tokenRepo;

    Pattern EMAIL_REGEX = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    Duration TOKEN_VALID_DURATION = Duration.ofHours(24);

    Map<String, PendingUser> pendingUsers = Collections.synchronizedMap(new HashMap<>());
    private final TourMapper tourMapper;

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
                .orElseThrow(() -> new AppException(USER_NOT_FOUND, "Feedback không hợp lệ và đã bị xoá!"));

        userMapper.updateUser(user, request);
        user.setUpdatedAt(LocalDateTime.now());

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse getUserById(String id) {
        return userMapper.toUserResponse(
                userRepository.findById(id)
                        .orElseThrow(() -> new AppException(USER_NOT_FOUND, "Feedback không hợp lệ và đã bị xoá!")));
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

    public boolean updateAvatarByToken(String token, String avatarUrl) {
        String jwt = token.replace("Bearer ", "");
        String userId = jwtUtil.extractUserId(jwt);
        return userRepository.findById(userId).map(user -> {
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
            if (newPassword.equals(oldPassword)) {
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
                .orElseThrow(() -> new AppException(USER_NOT_FOUND, "Feedback không hợp lệ và đã bị xoá!"));

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
                .orElseThrow(() -> new AppException(USER_NOT_FOUND, "Feedback không hợp lệ và đã bị xoá!"));

        // Kiểm tra email đã được dùng bởi người khác chưa
        boolean emailUsedByAnotherUser = userRepository.existsByEmail(email) &&
                !email.equalsIgnoreCase(user.getEmail());

        if (emailUsedByAnotherUser) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_USED, "Feedback không hợp lệ và đã bị xoá!");
        }

        user.setUpdatedAt(LocalDateTime.now());
        user.setEmail(email);
        userRepository.save(user);
    }

    public void updatePhone(String username, String phone) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new AppException(USER_NOT_FOUND, "Feedback không hợp lệ và đã bị xoá!"));
        // Kiểm tra phonenumber đã được dùng bởi người khác chưa
        boolean phoneUsedByAnotherUser = userRepository.existsByPhoneNumber(phone)
                || phone.equalsIgnoreCase(user.getPhoneNumber());
        if (phoneUsedByAnotherUser) {
            throw new AppException(ErrorCode.PHONE_ALREADY_USED, "Feedback không hợp lệ và đã bị xoá!");
        } else {
            user.setPhoneNumber(phone);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    public void updateAddress(String username, String address) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new AppException(USER_NOT_FOUND, "Feedback không hợp lệ và đã bị xoá!"));
        user.setAddress(address);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public void updateDob(String username, String dob) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new AppException(USER_NOT_FOUND, "Feedback không hợp lệ và đã bị xoá!"));

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

        // List<CreditCard> creditCards = creditCardRepository.findAllByUser();
        List<CreditCard> creditCards = creditCardRepository.findAllByUser_UserId(userId);
        return creditCards.stream().map(creditCardMapper::toCreditCardResponse).toList();
    }

    public CreditCardResponse addCreditCard(String token, CreditCardRequest request) {
        String jwt = token.replace("Bearer ", "");
        String userId = jwtUtil.extractUserId(jwt);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(USER_NOT_FOUND, "Feedback không hợp lệ và đã bị xoá!"));
        CreditCard creditCard = creditCardMapper.toCreditCard(request);
        creditCard.setCreatedAt(LocalDateTime.now());
        creditCard.setUpdatedAt(LocalDateTime.now());
        creditCard.setUser(user);
        creditCardRepository.save(creditCard);
        return creditCardMapper.toCreditCardResponse(creditCard);
    }

    public void deleteCreditCard(String token, String cardId) {
        String jwt = token.replace("Bearer ", "");
        String userId = jwtUtil.extractUserId(jwt);

        // Tìm credit card theo cardId và userId để đảm bảo user chỉ xóa thẻ của mình
        CreditCard creditCard = creditCardRepository.findByCardIDAndUser_UserId(cardId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.OPERATION_NOT_ALLOWED,
                        "Credit card not found or you don't have permission to delete it"));

        creditCardRepository.delete(creditCard);
    }

    @Transactional
    public void lockAccount(String bearerToken, String userId) {
        ensureAdmin(bearerToken);

        User u = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(USER_NOT_FOUND, "Người dùng không tìm thấy"));

        String role = Optional.ofNullable(u.getRole()).map(String::trim).orElse("");
        if (!"USER".equalsIgnoreCase(role) && !"SUB_COMPANY".equalsIgnoreCase(role)) {
            throw new AppException(OPERATION_NOT_ALLOWED,
                    "Chỉ được phép khóa tài khoản có vai trò USER hoặc SUB_COMPANY");
        }

        if (!"LOCKED".equalsIgnoreCase(u.getStatus())) {
            u.setStatus("LOCKED");
            u.setUpdatedAt(LocalDateTime.now());
            userRepository.save(u);
        }
    }

    @Transactional
    public void unlockAccount(String bearerToken, String userId) {
        ensureAdmin(bearerToken);

        User u = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(USER_NOT_FOUND, "Người dùng không tìm thấy"));

        String role = Optional.ofNullable(u.getRole()).map(String::trim).orElse("");
        if (!"USER".equalsIgnoreCase(role) && !"SUB_COMPANY".equalsIgnoreCase(role)) {
            throw new AppException(OPERATION_NOT_ALLOWED,
                    "Chỉ được phép mở khóa tài khoản có vai trò USER hoặc SUB_COMPANY");
        }

        if (!"ACTIVE".equalsIgnoreCase(u.getStatus())) {
            u.setStatus("ACTIVE");
            u.setUpdatedAt(LocalDateTime.now());
            userRepository.save(u);
        }
    }

    // Helper chung
    private void ensureAdmin(String bearerToken) {
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            throw new AppException(OPERATION_NOT_ALLOWED, "Token không hợp lệ");
        }
        String jwt = bearerToken.substring(7).trim();
        jwtUtil.validateToken(jwt);
        CustomUserDetails user = jwtUtil.getUserDetailsFromToken(jwt);
        if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new AppException(OPERATION_NOT_ALLOWED, "Chỉ ADMIN mới có quyền này");
        }
    }

    public List<TourResponse> getFavoritesByToken(String bearerToken) {
        String jwt = bearerToken.replace("Bearer ", "");
        String userId = jwtUtil.extractUserId(jwt);
        List<TourFavorite> favorites = tourFavoriteRepository.findByUser_UserId(userId);

        return favorites.stream()
                .map(tourFavorite -> tourMapper.toTourResponse(tourFavorite.getTour()))
                .toList();
    }

    public boolean removeFavoriteByToken(String bearerToken, String tourId) {
        String jwt = bearerToken.replace("Bearer ", "");
        String userId = jwtUtil.extractUserId(jwt);

        // Tìm favorite theo userId và tourId
        Optional<TourFavorite> favoriteOpt = tourFavoriteRepository.findByUser_UserIdAndTour_TourId(userId, tourId);
        if (favoriteOpt.isPresent()) {
            tourFavoriteRepository.delete(favoriteOpt.get());
            return true;
        }
        return false;
    }

    public boolean addFavoriteByToken(String bearerToken, String tourId) {
        String jwt = bearerToken.replace("Bearer ", "");
        String userId = jwtUtil.extractUserId(jwt);

        // Kiểm tra đã có favorite chưa
        Optional<TourFavorite> favoriteOpt = tourFavoriteRepository.findByUser_UserIdAndTour_TourId(userId, tourId);
        if (favoriteOpt.isPresent()) {
            return false; // Đã có trong favorites
        }
        // Lấy user và tour
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Tour> tourOpt = tourRepository.findTourByTourId(tourId); // hoặc
        // tourRepository.findById(tourId)
        if (userOpt.isPresent() && tourOpt.isPresent()) {

            TourFavoriteId favoriteId = new TourFavoriteId();
            favoriteId.setTourId(tourId);
            favoriteId.setUserId(userId);

            TourFavorite favorite = new TourFavorite();
            favorite.setId(favoriteId);
            favorite.setUser(userOpt.get());
            favorite.setTour(tourOpt.get());
            tourFavoriteRepository.save(favorite);
            return true;
        }
        return false;
    }

    public void updateProfile(String bearerToken, UserUpdateRequest req) {
        String jwt = bearerToken.replace("Bearer ", "");
        String userId = jwtUtil.extractUserId(jwt);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(USER_NOT_FOUND, "Feedback không hợp lệ và đã bị xoá!"));

        if (req.getEmail() != null) {
            // Kiểm tra email đã được dùng bởi người khác chưa
            boolean emailUsedByAnotherUser = userRepository.existsByEmail(req.getEmail()) &&
                    !req.getEmail().equalsIgnoreCase(user.getEmail());
            if (emailUsedByAnotherUser) {
                throw new AppException(ErrorCode.EMAIL_ALREADY_USED, "Feedback không hợp lệ và đã bị xoá!");
            }
            user.setEmail(req.getEmail());
        }

        if (req.getPhoneNumber() != null) {
            boolean phoneUsedByAnotherUser = userRepository.existsByPhoneNumber(req.getPhoneNumber()) &&
                    !req.getPhoneNumber().equalsIgnoreCase(user.getPhoneNumber());
            if (phoneUsedByAnotherUser) {
                throw new AppException(ErrorCode.PHONE_ALREADY_USED, "Feedback không hợp lệ và đã bị xoá!");
            }
            user.setPhoneNumber(req.getPhoneNumber());
        }

        if (req.getFirstName() != null)
            user.setFirstName(req.getFirstName());
        if (req.getLastName() != null)
            user.setLastName(req.getLastName());
        if (req.getAddress() != null)
            user.setAddress(req.getAddress());
        if (req.getDob() != null) {
            try {
                LocalDateTime parsedDate = req.getDob();
                user.setDob(parsedDate);
            } catch (Exception e) {
                throw new AppException(ErrorCode.INVALID_DATE_FORMAT, "Feedback không hợp lệ và đã bị xoá!");
            }
        }
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    // Code của P để lấy userId
    public UserResponse getUserFromToken(String token) {
        // Tách "Bearer " nếu có
        String tokenValue = token.startsWith("Bearer ") ? token.substring(7) : token;

        // 1. Kiểm tra phiên đăng nhập còn hợp lệ
        TokenAuthentication session = tokenRepo.findByTokenValue(tokenValue);
        if (session == null || !session.getIsUsed()) {
            throw new AppException(ErrorCode.SESSION_EXPIRED, "Phiên đăng nhập không hợp lệ hoặc đã hết hạn!");
        }

        // 2. Giải mã token lấy userId
        String userId = jwtUtil.extractUserId(tokenValue);

        // 3. Lấy user từ DB
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(USER_NOT_FOUND, "Không tìm thấy người dùng!"));

        // 4. Trả về DTO response
        return userMapper.toUserResponse(user);
    }

    public List<BookingTourResponse> getAllBooking(String token){
        String tokenValue = token.startsWith("Bearer ") ? token.substring(7) : token;

        // 1. Kiểm tra phiên đăng nhập còn hợp lệ
        TokenAuthentication session = tokenRepo.findByTokenValue(tokenValue);
        if (session == null || !session.getIsUsed()) {
            throw new AppException(ErrorCode.SESSION_EXPIRED, "Phiên đăng nhập không hợp lệ hoặc đã hết hạn!");
        }

        // 2. Giải mã token lấy userId
        String userId = jwtUtil.extractUserId(tokenValue);

//        List<BookingTour> bookingTourList = bookingTourRepository.findBookingToursByUser_UserId(userId);
        List<BookingTour> bookingTourList = bookingTourRepository.findBookingToursByUser_UserId(userId).stream().toList();

        return bookingTourList.stream()
                .map(tourBookingMapper::toBookingTourResponse)
                .collect(Collectors.toList());
    }
}