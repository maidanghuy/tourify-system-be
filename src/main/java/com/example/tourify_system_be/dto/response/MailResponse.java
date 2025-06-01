package com.example.tourify_system_be.dto.response;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MailResponse {
    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid recipient email")
    String to;

    @NotBlank(message = "Subject is required")
    String subject;

    @NotBlank(message = "Email body is required")
    String body;
}
