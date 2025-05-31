package com.example.tourify_system_be.service;

import com.example.tourify_system_be.dto.request.UserUpdateRequest;
import com.example.tourify_system_be.dto.response.UserResponse;
import com.example.tourify_system_be.entity.User;
import com.example.tourify_system_be.exception.AppException;
import com.example.tourify_system_be.exception.ErrorCode;
import com.example.tourify_system_be.mapper.UserMapper;
import com.example.tourify_system_be.repository.IUserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    IUserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

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
}
