package com.example.tourify_system_be.service;

import com.example.tourify_system_be.dto.request.SuggestTourRequest;
import com.example.tourify_system_be.dto.response.SuggestTourResponse;
import com.example.tourify_system_be.repository.IActivityRepository;
import com.example.tourify_system_be.repository.IServicesRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

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

    public SuggestTourResponse suggestTourFromImage(MultipartFile image) {
        // Lấy ngẫu nhiên services & activities
        List<String> serviceIds = serviceRepository.findRandomIds(4);
        List<String> activityIds = activityRepository.findRandomIds(4);

        String prompt = """
    Analyze the uploaded travel image and suggest:
    - A creative tour name
    - A short description
    - Estimated duration in days (1-7)
    - Place: return ONLY the official Vietnamese city/province name with correct accents (ví dụ: "Đà Lạt", "Hội An", "TP. Hồ Chí Minh")
    - Category: (romantic, adventure, cultural, etc.)

    IMPORTANT:
    Reply ONLY in pure JSON, no markdown, no explanation.
    Do not include any extra words.

    JSON format:
    {
      "tourName": "...",
      "description": "...",
      "duration": 3,
      "place": "...",
      "category": "..."
    }
    """;


        try {
            ByteArrayResource resource = new ByteArrayResource(image.getBytes());

            String aiText = chatClient.prompt()
                    .user(u -> u
                            .media(MimeTypeUtils.IMAGE_JPEG, resource)
                            .text(prompt)
                    )
                    .call()
                    .content();

            // Làm sạch kết quả
            aiText = aiText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            // Nếu AI trả về lẫn giải thích, chỉ lấy phần từ { đến }
            if (aiText.contains("{") && aiText.contains("}")) {
                aiText = aiText.substring(aiText.indexOf("{"), aiText.lastIndexOf("}") + 1);
            }

            // Mặc định
            String tourName = "AI Tour";
            String description = aiText;
            String place = "";
            String category = "";
            int duration = 3;

            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(aiText);

                if (node.has("tourName")) {
                    tourName = node.get("tourName").asText();
                }
                if (node.has("description")) {
                    description = node.get("description").asText();
                }
                if (node.has("duration")) {
                    duration = node.get("duration").asInt(3);
                }
                if (node.has("place")) {
                    place = node.get("place").asText();
                }
                if (node.has("category")) {
                    category = node.get("category").asText();
                }
            } catch (Exception e) {
                System.err.println("Parse JSON error: " + e.getMessage());
            }

            return SuggestTourResponse.builder()
                    .tourName(tourName)
                    .description(description)
                    .price(duration * 200)
                    .place(place)
                    .category(category)
                    .serviceIds(serviceIds)
                    .activityIds(activityIds)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to process image", e);
        }
    }
}
