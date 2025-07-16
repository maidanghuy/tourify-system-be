package com.example.tourify_system_be.mapper;

import com.example.tourify_system_be.dto.response.NotificationResponse;
import com.example.tourify_system_be.entity.UserNotification;

public class NotificationMapper {
    public static NotificationResponse toResponse(UserNotification un) {
        var n = un.getNotification();
        return new NotificationResponse(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.getType(),
                n.getLink(),
                n.getImageUrl(),
                n.getCreatedAt(),
                n.getCreatedBy(),
                un.getIsRead());
    }
}