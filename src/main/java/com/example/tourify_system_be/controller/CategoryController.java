package com.example.tourify_system_be.controller;

import com.example.tourify_system_be.entity.Category;
import com.example.tourify_system_be.repository.ICategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryRepository categoryRepository;

    @GetMapping("/api/categories")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll(); // Bạn có thể thêm filter .findByStatus("active")
    }
}
