package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.ChatMessage;
import com.example.tourify_system_be.entity.Message;
import com.example.tourify_system_be.entity.User;
import com.example.tourify_system_be.repository.IMessageRepository;
import com.example.tourify_system_be.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatRealTimeController {
    private final SimpMessagingTemplate messagingTemplate;
    @Autowired
    private IUserRepository userRepository; // hoặc UserService
    @Autowired
    private IMessageRepository messageRepository;


    @MessageMapping("/chat.private.send")
    public void sendPrivateMessage(ChatMessage message, Principal principal) {
        if (principal == null) throw new RuntimeException("Principal is null!");
        String senderId = principal.getName();
        message.setSenderId(senderId);

        // Lấy userName từ DB
        String userName = userRepository.findById(senderId)
                .map(User::getUserName)
                .orElse("Unknown");
        message.setSenderName(userName);

        // 1. LƯU MESSAGE VÀO DB!
        Message msgEntity = new Message();
        msgEntity.setMessageId(UUID.randomUUID().toString());
        msgEntity.setSenderId(senderId);
        msgEntity.setReceiverId(message.getReceiverId());
        msgEntity.setContent(message.getContent());
        msgEntity.setSentAt(LocalDateTime.now());
        msgEntity.setIsRead(false); // default
        messageRepository.save(msgEntity);

        // 2. GỬI REALTIME
        messagingTemplate.convertAndSendToUser(
                message.getReceiverId(),
                "/queue/messages",
                message
        );
    }



}

