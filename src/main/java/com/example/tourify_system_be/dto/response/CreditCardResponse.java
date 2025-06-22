package com.example.tourify_system_be.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreditCardResponse {
    String cardNumber;
    String cardHolderName;
    LocalDateTime expiryTime;
    String cardType;
}
