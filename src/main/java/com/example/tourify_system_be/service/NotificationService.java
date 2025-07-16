package com.example.tourify_system_be.service;

import com.example.tourify_system_be.dto.request.NotificationRequest;
import com.example.tourify_system_be.dto.response.NotificationResponse;
import com.example.tourify_system_be.entity.Notification;
import com.example.tourify_system_be.entity.User;
import com.example.tourify_system_be.entity.UserNotification;
import com.example.tourify_system_be.entity.UserNotificationId;
import com.example.tourify_system_be.mapper.NotificationMapper;
import com.example.tourify_system_be.repository.INotificationRepository;
import com.example.tourify_system_be.repository.IUserNotificationRepository;
import com.example.tourify_system_be.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final IUserRepository userRepo;
    private final INotificationRepository notificationRepo;
    private final IUserNotificationRepository userNotificationRepo;

    private final SimpMessagingTemplate messagingTemplate; // dùng để gửi WebSocket

    public List<NotificationResponse> getAllNotificationsForUser(String userId) {
        List<UserNotification> userNotifications = userNotificationRepo.findByUser_UserId(userId);
        List<NotificationResponse> notifications = userNotifications.stream()
                .map(NotificationMapper::toResponse)
                .collect(Collectors.toList());
        return notifications;
    }

    public void sendToAllUsers(NotificationRequest request) {
        Notification notification = Notification.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .link(request.getLink())
                .imageUrl(request.getImageUrl())
                .createdAt(LocalDateTime.now())
                .createdBy(request.getCreatedBy())
                .isDeleted(false)
                .build();

        notificationRepo.save(notification);

        List<User> users = userRepo.findAll();
        List<UserNotification> entries = new ArrayList<>();

        for (User user : users) {
            UserNotification un = UserNotification.builder()
                    .id(new UserNotificationId(user.getUserId(), notification.getId()))
                    .user(user)
                    .notification(notification)
                    .isRead(false)
                    .build();
            entries.add(un);
        }

        userNotificationRepo.saveAll(entries);
        messagingTemplate.convertAndSend("/topic/notifications", notification);

    }

    public void markAsRead(List<String> notificationIds, String userId) {
        for (String notiId : notificationIds) {
            UserNotificationId id = new UserNotificationId(userId, notiId);
            userNotificationRepo.findById(id).ifPresent(un -> {
                un.setIsRead(true);
                userNotificationRepo.save(un);
            });
        }
    }
    public void sendToUsers(NotificationRequest request) {
        Notification notification = Notification.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .link(request.getLink())
                .imageUrl(request.getImageUrl())
                .createdAt(LocalDateTime.now())
                .createdBy(request.getCreatedBy())
                .isDeleted(false)
                .build();

        notificationRepo.save(notification);

        List<User> users = userRepo.findAllById(request.getTargetUserIds());
        List<UserNotification> entries = new ArrayList<>();

        for (User user : users) {
            UserNotification un = UserNotification.builder()
                    .id(new UserNotificationId(user.getUserId(), notification.getId()))
                    .user(user)
                    .notification(notification)
                    .isRead(false)
                    .build();
            entries.add(un);

            // Nếu dùng WebSocket thì gửi ngay
            messagingTemplate.convertAndSend("/topic/user/" + user.getUserId(), notification);
        }

        userNotificationRepo.saveAll(entries);
    }
}
