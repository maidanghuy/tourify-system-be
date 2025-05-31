package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.APIResponse;
import com.example.tourify_system_be.dto.request.ChangePasswordRequest;
import com.example.tourify_system_be.dto.request.UpdateAvatarRequest;
import com.example.tourify_system_be.dto.request.UserUpdateRequest;
import com.example.tourify_system_be.dto.response.UserResponse;
import com.example.tourify_system_be.entity.User;
import com.example.tourify_system_be.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")

public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("")
    public APIResponse<List<UserResponse>> getAllUsers(){
        return APIResponse.<List<UserResponse>>builder()
                .result(userService.getUsers())
                .build();
    }

    @PutMapping("/{userId}")
    public APIResponse<UserResponse> updateUser(@PathVariable("userId") String userId, @RequestBody UserUpdateRequest request){
        return APIResponse.<UserResponse>builder()
                .result(userService.updateUser(userId, request))
                .build();
    }
    /*
    Sample JSON:
    {
          "userName": "johndoe",
          "firstName": "John",
          "lastName": "Doe",
          "phoneNumber": "0987654321",
          "gender": true,
          "address": "123 Main Street, City",
          "dob": "2004-08-31T00:00:00Z",
          "status": "active",
          "socialLink": "https://facebook.com/johndoe",
          "avatar": "https://example.com/avatar.jpg",
          "background": "https://example.com/background.jpg"
      }
    */

    @GetMapping("/{userId}")
    public APIResponse<UserResponse> getUserById(@PathVariable("userId") String id){
        return APIResponse.<UserResponse>builder()
                .result(userService.getUserById(id))
                .build();
    }

    @PutMapping("/avatar")
    public ResponseEntity<?> updateAvatar(
            @RequestParam String username,
            @RequestBody UpdateAvatarRequest request) {

        boolean updated = userService.updateAvatar(username, request.getAvatar());
        if (updated) {
            return ResponseEntity.ok("Avatar updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {
        String result = userService.changePassword(
                request.getUsername(),
                request.getOldPassword(),
                request.getNewPassword(),
                request.getConfirmPassword());

        if ("Password changed successfully".equals(result)) {
            return ResponseEntity.ok(result);
        } else if ("Username does not exist".equals(result)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
    }
}
