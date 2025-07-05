package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.APIResponse;
import com.example.tourify_system_be.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/useradmins")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserService userService;

    /**
     * Khoá tài khoản (chỉ ADMIN).
     */
    @PutMapping("/{id}/lock")
    public ResponseEntity<APIResponse<Void>> lockAccount(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable("id") String userId) {

        userService.lockAccount(authorization, userId);

        APIResponse<Void> body = APIResponse.<Void>builder()
                .code(0)
                .message("User has been locked successfully")
                .build();

        return ResponseEntity.ok(body);
    }

    /**
     * Mở khoá tài khoản (chỉ ADMIN).
     */
    @PutMapping("/{id}/unlock")
    public ResponseEntity<APIResponse<Void>> unlockAccount(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @PathVariable("id") String userId) {

        userService.unlockAccount(authorization, userId);

        APIResponse<Void> body = APIResponse.<Void>builder()
                .code(0)
                .message("User has been unlocked successfully")
                .build();

        return ResponseEntity.ok(body);
    }
}
