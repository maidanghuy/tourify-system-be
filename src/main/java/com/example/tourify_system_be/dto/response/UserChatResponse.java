package com.example.tourify_system_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserChatResponse {
    private String userId;
    private String userName;
    private String avatar;
    private boolean online;
}
