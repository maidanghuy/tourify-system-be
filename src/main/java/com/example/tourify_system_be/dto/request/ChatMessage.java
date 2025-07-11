package com.example.tourify_system_be.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private String senderId;    // Server tự lấy từ access token
    private String receiverId;  // Người nhận (frontend gửi lên)
    private String content;
    private String senderName;
}

