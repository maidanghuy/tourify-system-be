package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.UserNotification;
import com.example.tourify_system_be.entity.UserNotificationId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IUserNotificationRepository extends JpaRepository<UserNotification, UserNotificationId> {
    List<UserNotification> findByUser_UserIdAndIsReadFalse(String userId);
    List<UserNotification> findByUser_UserId(String userId);
}

