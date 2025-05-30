package com.example.tourify_system_be.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthResetPasswordRequest {
    String token;
    String newPassword;
    String confirmPassword;
}
