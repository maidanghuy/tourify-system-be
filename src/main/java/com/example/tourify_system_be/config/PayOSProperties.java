package com.example.tourify_system_be.config;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PayOSProperties {
    String clientId;
    String apiKey;
    String checksumKey;
    String returnUrl;
    String cancelUrl;
}

