package com.example.tourify_system_be.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {
     String username;
     String oldPassword;
    @Size(min = 6, message = "New password must be at least 6 characters long")
     String newPassword;
     String confirmPassword;  // thêm trường này
}
