package com.example.tourify_system_be.service;

import com.example.tourify_system_be.entity.Category;
import com.example.tourify_system_be.repository.ICategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final ICategoryRepository categoryRepository;

    @Autowired
    public CategoryService(ICategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Trả về danh sách tên tất cả thể loại, sắp xếp theo thứ tự ABC.
     */
    public List<String> getAllCategoryNames() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "categoryName"))
                .stream()
                .map(Category::getCategoryName)
                .collect(Collectors.toList());
    }

    /**
     * Trả về danh sách tất cả thể loại, sắp xếp theo tên.
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "categoryName"));
    }

    /**
     * Trả về danh sách thể loại theo status (không phân biệt chữ hoa/thường),
     * sắp xếp theo tên.
     */
    public List<Category> getCategoriesByStatus(String status) {
        return categoryRepository.findByStatusIgnoreCase(status)
                .stream()
                .sorted(Comparator.comparing(Category::getCategoryName))
                .collect(Collectors.toList());
    }

    /**
     * Trả về danh sách tên thể loại theo status (không phân biệt chữ hoa/thường),
     * sắp xếp theo thứ tự ABC.
     */
    public List<String> getCategoryNamesByStatus(String status) {
        return getCategoriesByStatus(status)
                .stream()
                .map(Category::getCategoryName)
                .collect(Collectors.toList());
    }
}
