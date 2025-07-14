package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.*;
import com.example.tourify_system_be.dto.response.CreditCardResponse;
import com.example.tourify_system_be.dto.response.TourResponse;
import com.example.tourify_system_be.dto.response.UserResponse;
import com.example.tourify_system_be.entity.Tour;
import com.example.tourify_system_be.entity.TourFavorite;
import com.example.tourify_system_be.exception.AppException;
import com.example.tourify_system_be.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
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
                        @RequestHeader("Authorization") String token,
                        @RequestBody UpdateAvatarRequest request) {

                boolean updated = userService.updateAvatarByToken(token, request.getAvatar());
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
         * Sample JSON:
         * {
         * "avatar": "https://example.com/avatar.jpg"
         * }
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

        @PutMapping("/profile")
        public APIResponse<?> updateProfile(
                        @RequestHeader("Authorization") String token,
                        @Valid @RequestBody UserUpdateRequest request,
                        BindingResult bindingResult) {
                if (bindingResult.hasErrors()) {
                        String errorMsg = bindingResult.getFieldErrors().stream()
                                        .map(e -> e.getField() + ": " + e.getDefaultMessage())
                                        .reduce((a, b) -> a + "; " + b)
                                        .orElse("Invalid input");
                        return APIResponse.builder()
                                        .code(400)
                                        .message(errorMsg)
                                        .result(null)
                                        .build();
                }
                try {
                        userService.updateProfile(token, request);
                        return APIResponse.builder()
                                        .message("Update profile successfully")
                                        .code(1000)
                                        .build();
                } catch (AppException ex) {
                        return APIResponse.builder()
                                        .code(400)
                                        .message(ex.getMessage())
                                        .result(null)
                                        .build();
                } catch (Exception ex) {
                        return APIResponse.builder()
                                        .code(500)
                                        .message("Internal server error")
                                        .result(null)
                                        .build();
                }
        }

        @GetMapping("/creditcard")
        public APIResponse<?> getCreditCards(@RequestHeader("Authorization") String token) {
                return APIResponse.<List<CreditCardResponse>>builder()
                                .result(userService.getAllCreditCardByToken(token))
                                .build();
        }

        @PostMapping("/creditcard")
        public APIResponse<?> addCreditCard(@RequestHeader("Authorization") String token,
                        @Valid @RequestBody CreditCardRequest request) {
                return APIResponse.<CreditCardResponse>builder()
                                .result(userService.addCreditCard(token, request))
                                .build();
        }

        @DeleteMapping("/creditcard/{cardId}")
        public APIResponse<?> deleteCreditCard(@RequestHeader("Authorization") String token,
                        @PathVariable("cardId") String cardId) {
                userService.deleteCreditCard(token, cardId);
                return APIResponse.builder()
                                .message("Credit card deleted successfully")
                                .code(1000)
                                .result(null)
                                .build();
        }

        // Sample JSON
        // {
        // "cardNumber": ,
        // "cardHolderName": ,
        // "expiryTime": ,
        // "cardType":
        // }

        @GetMapping("/favorites")
        public APIResponse<?> getFavorites(@RequestHeader("Authorization") String token) {
                return APIResponse.<List<TourResponse>>builder()
                                .result(userService.getFavoritesByToken(token))
                                .build();
        }

        @DeleteMapping("/favorites/{tourId}")
        public APIResponse<?> removeFavorite(
                        @RequestHeader("Authorization") String token,
                        @PathVariable("tourId") String tourId) {
                boolean removed = userService.removeFavoriteByToken(token, tourId);
                if (removed) {
                        return APIResponse.builder()
                                        .message("Removed from favorites successfully")
                                        .code(1000)
                                        .result(null)
                                        .build();
                } else {
                        return APIResponse.builder()
                                        .message("Favorite not found or not removed")
                                        .code(4001)
                                        .result(null)
                                        .build();
                }
        }

        @PostMapping("/favorites/{tourId}")
        public APIResponse<?> addFavorite(
                        @RequestHeader("Authorization") String token,
                        @PathVariable("tourId") String tourId) {
                boolean added = userService.addFavoriteByToken(token, tourId);
                if (added) {
                        return APIResponse.builder()
                                        .message("Added to favorites successfully")
                                        .code(1000)
                                        .result(null)
                                        .build();
                } else {
                        return APIResponse.builder()
                                        .message("Already in favorites or failed to add")
                                        .code(4002)
                                        .result(null)
                                        .build();
                }
        }

        // lấy thông tin profiel (Phong)
        @GetMapping("/info")
        public APIResponse<?> getUserProfileFromToken(@RequestHeader("Authorization") String token) {
                return APIResponse.<UserResponse>builder()
                                .result(userService.getUserFromToken(token))
                                .build();
        }

}
