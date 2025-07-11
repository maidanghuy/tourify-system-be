package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.response.MessageResponse;
import com.example.tourify_system_be.dto.response.UserChatResponse;
import com.example.tourify_system_be.dto.response.UserResponse;
import com.example.tourify_system_be.repository.IUserRepository;
import com.example.tourify_system_be.security.JwtUtil;
import com.example.tourify_system_be.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;
    private final JwtUtil jwtUtil;
    private final IUserRepository userRepository; // Bổ sung

    @GetMapping("/users")
    public List<UserChatResponse> getChattedUsers(@RequestHeader("Authorization") String token) {
        String myUserId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        return messageService.getChattedUsers(myUserId);
    }


    @GetMapping("/history")
    public List<MessageResponse> getChatHistory(@RequestHeader("Authorization") String token,
                                                @RequestParam("with") String withUserId) {
        String myUserId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        return messageService.getChatHistory(myUserId, withUserId);
    }


}
