package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.ChatMessage;
import com.example.tourify_system_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatRealTimeController {
    private final SimpMessagingTemplate messagingTemplate;
    @Autowired
    private UserRepository userRepository; // hoặc UserService

    @MessageMapping("/chat.private.send")
    public void sendPrivateMessage(ChatMessage message, Principal principal) {
        if (principal == null) throw new RuntimeException("Principal is null!");
        String senderId = principal.getName();
        message.setSenderId(senderId);

        // Lấy userName từ DB
        String userName = userRepository.findById(senderId)
                .map(user -> user.getUserName())
                .orElse("Unknown");
        message.setSenderName(userName);

        messagingTemplate.convertAndSendToUser(
                message.getReceiverId(),
                "/queue/messages",
                message
        );
    }


}

