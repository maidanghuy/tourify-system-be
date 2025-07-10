package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.dto.request.APIResponse;
import com.example.tourify_system_be.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping("/upload")
    public APIResponse<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = cloudinaryService.uploadFile(file);

            Map<String, Object> response = new HashMap<>();
            response.put("url", imageUrl);
            response.put("fileName", file.getOriginalFilename());
            response.put("status", "success");
            return APIResponse.<Map<String, Object>>builder()
                    .code(1000)
                    .message("Upload successful")
                    .result(response)
                    .build();
        } catch (Exception e) {
            return APIResponse.<Map<String, Object>>builder()
                    .code(400)
                    .message("Upload failed")
                    .result(null)
                    .build();
        }
    }
}
