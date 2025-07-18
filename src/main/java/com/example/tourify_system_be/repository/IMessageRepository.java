package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IMessageRepository extends JpaRepository<Message, String> {
    List<Message> findBySenderIdOrReceiverId(String senderId, String receiverId);

    // THÊM HÀM MỚI, CHẮC CHẮN ĐÚNG:
    @Query("SELECT m FROM Message m " +
            "WHERE (m.senderId = :userId1 AND m.receiverId = :userId2) " +
            "   OR (m.senderId = :userId2 AND m.receiverId = :userId1) " +
            "ORDER BY m.sentAt ASC")
    List<Message> findAllMessagesBetweenUsers(
            @Param("userId1") String userId1,
            @Param("userId2") String userId2
    );
}

