package com.blogapp.service;

import com.blogapp.payload.CategoryDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {
    CategoryDto createCategory(CategoryDto categoryDto);
    String deleteCategory(Long categoryId);
    CategoryDto updateCategory(Long categoryId,CategoryDto categoryDto);
    List<CategoryDto> listOfCategorys();
    CategoryDto findCategorys(Long categoryId);
}
