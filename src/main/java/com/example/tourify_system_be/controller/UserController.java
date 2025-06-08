package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.*;
import com.example.tourify_system_be.dto.response.UserResponse;
import com.example.tourify_system_be.entity.User;
import com.example.tourify_system_be.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")

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
    /*
    Sample JSON:
    {
          "avatar": "https://example.com/avatar.jpg"
      }
    */

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

    @PutMapping("/name")
    public APIResponse<?> updateName(
            @RequestParam String username,
            @RequestBody UpdateNameRequest request
    )
    {
        userService.updateName(username, request.getFirstName(), request.getLastName());
        return APIResponse.builder()
                .message("Update name successfully")
                .build();
    }
    /*
    Sample JSON:
    {
        "firstName": "<FirstName>",
        "lastName": "<LastName>"
    }
    */

    @PutMapping("/email")
    public APIResponse<?> updateEmail(
            @RequestParam String username,
            @RequestBody UpdateEmailRequest request
    )
    {
        userService.updateEmail(username, request.getEmail());
        return APIResponse.builder()
                .message("Update email successfully")
                .build();
    }
    /*
    Sample JSON:
    {
        "email": "<EMAIL>"
    }
    */

    @PutMapping("/phone")
    public APIResponse<?> updatePhone(
            @RequestParam String username,
            @RequestBody UpdatePhoneRequest request
    )
    {
        userService.updatePhone(username, request.getPhoneNumber());
        return APIResponse.builder()
                .message("Update phone successfully")
                .build();
    }
    /*
    Sample JSON:
    {
        "phoneNumber": "0987654321"
    }
    */

    @PutMapping("/address")
    public APIResponse<?> updateAddress(
            @RequestParam String username,
            @RequestBody UpdateAddressRequest request
    )
    {
        userService.updateAddress(username, request.getAddress());
        return APIResponse.builder()
                .message("Update address successfully")
                .build();
    }
    /*
    Sample JSON:
    {
        "address": "123 Main Street, City"
    }
    */

    @PutMapping("/dob")
    public APIResponse<?> updateDob(
            @RequestParam String username,
            @RequestBody UpdateDobRequest request
    )
    {
        userService.updateDob(username, request.getDob());
        return APIResponse.builder()
                .message("Update dob successfully")
                .build();
    }
    /*
    Sample JSON:
    {
        "dob": "2004-08-31"
    }
    */
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
