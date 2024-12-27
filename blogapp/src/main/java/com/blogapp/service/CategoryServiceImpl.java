package com.blogapp.service;

import com.blogapp.entity.Category;
import com.blogapp.exception.CategoryAlreadyExistsException;
import com.blogapp.payload.CategoryDto;
import com.blogapp.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    public CategoryDto mapToDto(Category category) {
        return modelMapper.map(category, CategoryDto.class);
    }

    public Category mapToEntity(CategoryDto categoryDto) {
        return modelMapper.map(categoryDto, Category.class);
    }

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        logger.info("Attempting to create category with name: {}", categoryDto.getCategoryName());
        Optional<Category> opCategory;
        Category category = null;
        Category saved = null;
        try {
            opCategory = categoryRepository.findByCategoryName(categoryDto.getCategoryName());
            if (opCategory.isPresent()) {
                logger.warn("Category creation failed: Category '{}' already exists.", categoryDto.getCategoryName());
                throw new CategoryAlreadyExistsException("Category " + categoryDto.getCategoryName() + " already exists.");
            }
            category = mapToEntity(categoryDto);
            category.setCreateAt(LocalDateTime.now().withNano(0));
            category.setUpdateAt(LocalDateTime.now().withNano(0));
            saved = categoryRepository.save(category);
            logger.info("Category '{}' created successfully with ID: {}", categoryDto.getCategoryName(), saved.getId());
            return mapToDto(saved);
        } catch (Exception e) {
            logger.error("Failed to create category: {}", e.getMessage(), e);
            throw e;
        } finally {
            opCategory = Optional.empty();
            category = null;
            saved = null;
        }
    }

    @Override
    public String deleteCategory(Long categoryId) {
        logger.info("Attempting to delete category with ID: {}", categoryId);
        Optional<Category> opCategory;
        try {
            opCategory = categoryRepository.findById(categoryId);
            if (opCategory.isPresent()) {
                categoryRepository.deleteById(categoryId);
                logger.info("Category with ID: {} deleted successfully.", categoryId);
                return "Category is deleted by category id: " + categoryId;
            }
            logger.warn("Failed to delete category: Category with ID: {} not found.", categoryId);
            return "Category is not found!";
        } catch (Exception e) {
            logger.error("Failed to delete category: {}", e.getMessage(), e);
            throw e;
        } finally {
            opCategory = Optional.empty();
        }
    }

    @Override
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        logger.info("Attempting to update category with ID: {}", categoryId);
        Optional<Category> opCategory;
        Category ct = null;
        Category category = null;
        Category saved = null;
        try {
            opCategory = categoryRepository.findById(categoryId);
            if (opCategory.isPresent()) {
                ct = opCategory.get();
                category = mapToEntity(categoryDto);
                category.setId(categoryId);
                category.setCreateAt(ct.getCreateAt());
                category.setUpdateAt(LocalDateTime.now().withNano(0));
                saved = categoryRepository.save(category);
                logger.info("Category with ID: {} updated successfully.", categoryId);
                return mapToDto(saved);
            }
            logger.warn("Failed to update category: Category with ID: {} not found.", categoryId);
            return null;
        } catch (Exception e) {
            logger.error("Failed to update category: {}", e.getMessage(), e);
            throw e;
        } finally {
            opCategory = Optional.empty();
            ct = null;
            category = null;
            saved = null;
        }
    }

    @Override
    public List<CategoryDto> listOfCategorys() {
        logger.info("Fetching list of all categories.");
        List<Category> categoryList = null;
        List<CategoryDto> categoryDtoList = null;
        try {
            categoryList = categoryRepository.findAll();
            logger.info("Fetched {} categories from the database.", categoryList.size());
            categoryDtoList = categoryList.stream().map(this::mapToDto).collect(Collectors.toList());
            return categoryDtoList;
        } catch (Exception e) {
            logger.error("Failed to fetch categories: {}", e.getMessage(), e);
            throw e;
        } finally {
            categoryList = null;
            categoryDtoList = null;
        }
    }

    @Override
    public CategoryDto findCategorys(Long categoryId) {
        logger.info("Fetching category with ID: {}", categoryId);
        Optional<Category> opCategory;
        try {
            opCategory = categoryRepository.findById(categoryId);
            if (opCategory.isPresent()) {
                logger.info("Category with ID: {} found.", categoryId);
                return mapToDto(opCategory.get());
            }
            logger.warn("Category with ID: {} not found.", categoryId);
            return null;
        } catch (Exception e) {
            logger.error("Failed to fetch category: {}", e.getMessage(), e);
            throw e;
        } finally {
            opCategory = Optional.empty();
        }
    }
}
