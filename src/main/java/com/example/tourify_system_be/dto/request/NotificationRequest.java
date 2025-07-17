package com.example.tourify_system_be.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationRequest {
    String title;
    String message;
    String type;
    String link;
    String imageUrl;
    String createdBy;
    List<String> targetUserIds;
}