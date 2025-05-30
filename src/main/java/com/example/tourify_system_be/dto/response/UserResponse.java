package com.example.tourify_system_be.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String userId;
    String role;
    String userName;
    String firstName;
    String lastName;
    String email;
    String phoneNumber;
    Boolean gender;
    String address;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    LocalDateTime dob;
    String status;
    String socialLink;
    String avatar;
    String background;
}
