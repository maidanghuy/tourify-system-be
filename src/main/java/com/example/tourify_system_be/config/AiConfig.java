package com.example.tourify_system_be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.client.ChatClient;

@Configuration
public class AiConfig {

    private final ChatClient.Builder chatClientBuilder;

    // Spring AI sẽ tự inject ChatClient.Builder
    public AiConfig(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    @Bean
    public ChatClient chatClient() {
        return chatClientBuilder.build();
    }
}
