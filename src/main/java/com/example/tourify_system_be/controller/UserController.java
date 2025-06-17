package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.*;
import com.example.tourify_system_be.dto.response.UserResponse;
import com.example.tourify_system_be.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("")
    public APIResponse<List<UserResponse>> getAllUsers() {
        return APIResponse.<List<UserResponse>>builder()
                .result(userService.getUsers())
                .build();
    }

    @GetMapping("/{userId}")
    public APIResponse<UserResponse> getUserById(@PathVariable("userId") String id) {
        return APIResponse.<UserResponse>builder()
                .result(userService.getUserById(id))
                .build();
    }

    @PutMapping("/{userId}")
    public APIResponse<UserResponse> updateUser(
            @PathVariable("userId") String userId,
            @RequestBody UserUpdateRequest request) {
        return APIResponse.<UserResponse>builder()
                .result(userService.updateUser(userId, request))
                .build();
    }

    @PutMapping("/avatar")
    public APIResponse<?> updateAvatar(
            @RequestParam String username,
            @RequestBody UpdateAvatarRequest request) {

        boolean updated = userService.updateAvatar(username, request.getAvatar());
        if (updated) {
            return APIResponse.<Void>builder()
                    .message("Avatar updated successfully")
                    .result(null)
                    .build();
        } else {
            return APIResponse.<Void>builder()
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Avatar update failed")
                    .result(null)
                    .build();
        }
    }

    @PostMapping("/change-password")
    public APIResponse<?> changePassword(@RequestBody ChangePasswordRequest request) {
        String result = userService.changePassword(
                request.getUsername(),
                request.getOldPassword(),
                request.getNewPassword(),
                request.getConfirmPassword());

        if ("Password changed successfully".equals(result)) {
            return APIResponse.builder()
                    .message(result)
                    .result(null)
                    .code(HttpStatus.OK.value())
                    .build();
        } else if ("Username does not exist".equals(result)) {
            return APIResponse.builder()
                    .message(result)
                    .result(null)
                    .code(HttpStatus.NOT_FOUND.value())
                    .build();
        } else {
            return APIResponse.builder()
                    .message(result)
                    .result(null)
                    .code(HttpStatus.BAD_REQUEST.value())
                    .build();
        }
    }

    @PutMapping("/{userId}/lock")
    public ResponseEntity<String> lockUser(@PathVariable String userId) {
        userService.updateAccountStatus(userId, "locked");
        return ResponseEntity.ok("User locked successfully.");
    }

    @PutMapping("/{userId}/unlock")
    public ResponseEntity<String> unlockUser(@PathVariable String userId) {
        userService.updateAccountStatus(userId, "active");
        return ResponseEntity.ok("User unlocked successfully.");
    }
}
