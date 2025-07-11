package com.example.tourify_system_be.config;

import com.example.tourify_system_be.security.JwtUtil;
import com.example.tourify_system_be.security.StompPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {
    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                try {
                    String userId = jwtUtil.extractUserId(jwt);
                    String username = jwtUtil.extractUsername(jwt);
                    accessor.setUser(new StompPrincipal(userId, username)); // Gán principal
                    System.out.println("JwtChannelInterceptor gán userId: " + userId);
                } catch (Exception ex) {
                    System.out.println("JWT ERROR: " + ex.getMessage());
                }
            } else {
                System.out.println("JwtChannelInterceptor: Không tìm thấy Authorization header hoặc sai định dạng!");
            }
        }
        return message;
    }
}

