package com.example.tourify_system_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MessageResponse {
    private String messageId;
    private String senderId;
    private String senderName;
    private String receiverId;
    private String content;
    private LocalDateTime sentAt;
    private Boolean isRead;
}
