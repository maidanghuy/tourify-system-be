package com.example.tourify_system_be.repository;

import com.example.tourify_system_be.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IMessageRepository extends JpaRepository<Message, String> {
    List<Message> findBySenderIdOrReceiverId(String senderId, String receiverId);

    List<Message> findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderBySentAtAsc(
            String senderId1, String receiverId1,
            String senderId2, String receiverId2
    );
}

