package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.Notification;
import com.example.tourify_system_be.entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface INotificationRepository extends JpaRepository<Notification, String> {
}
