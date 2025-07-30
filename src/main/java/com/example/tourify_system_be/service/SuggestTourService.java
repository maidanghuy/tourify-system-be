package com.example.tourify_system_be.service;

import com.example.tourify_system_be.dto.request.SuggestTourRequest;
import com.example.tourify_system_be.dto.response.SuggestTourResponse;
import com.example.tourify_system_be.repository.IActivityRepository;
import com.example.tourify_system_be.repository.IServicesRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SuggestTourService {

    private final ChatClient chatClient; // từ Spring AI
    private final IServicesRepository serviceRepository;
    private final IActivityRepository activityRepository;

    public SuggestTourResponse suggestTour(SuggestTourRequest req) {

        // 1. Random serviceIds và activityIds từ DB (String vì là UUID)
        List<String> serviceIds = serviceRepository.findRandomIds(4);
        List<String> activityIds = activityRepository.findRandomIds(4);

        // 2. Prompt AI rõ ràng: chỉ trả về JSON
        String prompt = """
            You are a travel assistant.
            Generate a creative travel tour name and a short description as a pure JSON object. 
            IMPORTANT:
            - Do NOT include ```json or any markdown.
            - Do NOT include any explanations, ONLY return JSON.

            Information:
            - Place: %s
            - Category: %s
            - Duration: %d days

            JSON format:
            {
              "tourName": "...",
              "description": "..."
            }
            """.formatted(req.getPlace(), req.getCategory(), req.getDuration());

        // 3. Gọi AI qua Spring AI
        String aiText = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // 4. Làm sạch kết quả: loại bỏ dấu ``` nếu AI vẫn trả về markdown
        aiText = aiText.replace("```json", "")
                .replace("```", "")
                .trim();

        // 5. Parse JSON
        String tourName = req.getDuration() + " Day " + req.getPlace();
        String description = aiText;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(aiText);

            if (node.has("tourName") && node.has("description")) {
                tourName = node.get("tourName").asText();
                description = node.get("description").asText();
            }
        } catch (Exception e) {
            // Nếu parse thất bại, fallback: giữ nguyên description = raw AI text
            System.err.println("Failed to parse AI JSON: " + e.getMessage());
        }

        // 6. Trả kết quả
        return SuggestTourResponse.builder()
                .tourName(tourName)
                .description(description)
                .price(req.getDuration() * 200)
                .serviceIds(serviceIds)
                .activityIds(activityIds)
                .build();
    }
}
