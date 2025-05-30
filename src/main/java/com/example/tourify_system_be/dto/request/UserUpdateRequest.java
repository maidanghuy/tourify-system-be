package com.example.tourify_system_be.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String userName;
    String firstName;
    String lastName;
    String phoneNumber;
    Boolean gender;
    String address;
    LocalDateTime dob;
    String status;
    String socialLink;
    String avatar;
    String background;
}
