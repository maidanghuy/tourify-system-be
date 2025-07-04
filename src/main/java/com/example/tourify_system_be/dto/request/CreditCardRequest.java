package com.example.tourify_system_be.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreditCardRequest {
    String cardNumber;
    String cardHolderName;
    LocalDateTime expiryTime;
    String cardType;
}
