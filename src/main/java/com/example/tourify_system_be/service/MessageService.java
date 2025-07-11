package com.example.tourify_system_be.service;

import com.example.tourify_system_be.dto.response.MessageResponse;
import com.example.tourify_system_be.dto.response.UserChatResponse;
import com.example.tourify_system_be.entity.Message;
import com.example.tourify_system_be.entity.User;
import com.example.tourify_system_be.repository.IMessageRepository;
import com.example.tourify_system_be.repository.ITokenAuthenticationRepository;
import com.example.tourify_system_be.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final IMessageRepository messageRepository;
    private final IUserRepository userRepository;
    private final ITokenAuthenticationRepository tokenAuthRepository;

    public boolean isUserOnline(String userId) {
        // Trả về true nếu có token còn hiệu lực và is_used = 1
        return tokenAuthRepository.existsByUser_UserIdAndIsUsedAndExpiresAtAfter(
                userId, true, LocalDateTime.now()
        );
    }


    // Lấy list user từng chat (dùng cho API /api/messages/users)
    public List<UserChatResponse> getChattedUsers(String myUserId) {
        List<Message> all = messageRepository.findBySenderIdOrReceiverId(myUserId, myUserId);
        Set<String> userIds = new HashSet<>();
        for (Message msg : all) {
            if (!msg.getSenderId().equals(myUserId)) userIds.add(msg.getSenderId());
            if (!msg.getReceiverId().equals(myUserId)) userIds.add(msg.getReceiverId());
        }
        // Lấy thêm avatar, userName từ DB
        List<User> users = userRepository.findAllById(userIds);
        return users.stream()
                .map(u -> new UserChatResponse(u.getUserId(), u.getUserName(), u.getAvatar(),isUserOnline(u.getUserId())))
                .collect(Collectors.toList());
    }

    // Lấy lịch sử chat (dùng cho API /api/messages/history)
    public List<MessageResponse> getChatHistory(String myUserId, String otherUserId) {
        List<Message> msgs = messageRepository.findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderBySentAtAsc(
                myUserId, otherUserId, otherUserId, myUserId
        );
        return msgs.stream().map(m -> new MessageResponse(
                m.getMessageId(),
                m.getSenderId(),
                // Optional: show tên người gửi
                userRepository.findById(m.getSenderId()).map(User::getUserName).orElse(""),
                m.getReceiverId(),
                m.getContent(),
                m.getSentAt(),
                m.getIsRead()
        )).collect(Collectors.toList());
    }
}
