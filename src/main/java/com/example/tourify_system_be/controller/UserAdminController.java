package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.APIResponse;
import com.example.tourify_system_be.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/useradmins")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserService userService;

    @PostMapping("/{id}/lock")
    public APIResponse<Void> lock(@RequestHeader("Authorization") String token, @PathVariable("id") String id) {
        userService.lockAccount(token,id);
        return APIResponse.<Void>builder()
                .code(0)
                .message("User locked")
                .build();
    }

    @PostMapping("/{id}/unlock")
    public APIResponse<Void> unlock(@RequestHeader("Authorization") String token, @PathVariable("id") String id) {
        userService.unlockAccount(token,id);
        return APIResponse.<Void>builder()
                .code(0)
                .message("User unlocked")
                .build();
    }
}
