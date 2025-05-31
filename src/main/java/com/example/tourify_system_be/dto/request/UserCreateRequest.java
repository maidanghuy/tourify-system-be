package com.example.tourify_system_be.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreateRequest {

    String role; // Mặc định là "user" nếu không truyền

    @NotBlank(message = "Username không được để trống")
    String userName;

    @NotBlank(message = "firstname không được để trống")
    String firstName;

    @NotBlank(message = "lastname không được để trống")
    String lastName;

    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    String email;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Password phải có ít nhất 6 ký tự")
    String password;

    @NotBlank(message = "Confirm Password không được để trống")
    String passwordConfirm;

    // Cho phép null. Nếu null thì backend sẽ mặc định là false (nam)
    Boolean gender;
}
