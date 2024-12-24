package com.blogapp.service;

import com.blogapp.entity.Category;
import com.blogapp.exception.CategoryAlreadyExistsException;
import com.blogapp.payload.CategoryDto;
import com.blogapp.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService{

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    public CategoryDto mapToDto(Category category){
        return modelMapper.map(category,CategoryDto.class);
    }

    public Category mapToEntity(CategoryDto categoryDto){
        return modelMapper.map(categoryDto,Category.class);
    }

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {



        Optional<Category> opCategory = categoryRepository.findByCategoryName(categoryDto.getCategoryName());
        if(opCategory.isPresent()){
            throw new CategoryAlreadyExistsException("Category " + categoryDto.getCategoryName() + " already exists.");
        }
        Category category = mapToEntity(categoryDto);
        category.setCreateAt(LocalDateTime.now().withNano(0));
        category.setUpdateAt(LocalDateTime.now().withNano(0));
        Category saved = categoryRepository.save(category);
        return mapToDto(saved);
    }

    @Override
    public String deleteCategory(Long categoryId) {
        Optional<Category> opCategory = categoryRepository.findById(categoryId);
        if(opCategory.isPresent()){
            categoryRepository.deleteById(categoryId);
            return "Category is deleted by category id : "+categoryId;
        }
        return "Category is not found!";
    }

    @Override
    public CategoryDto updateCategory(Long categoryId,CategoryDto categoryDto) {
        Optional<Category> opCategory = categoryRepository.findById(categoryId);
        if(opCategory.isPresent()){
            Category ct = opCategory.get();
            Category category = mapToEntity(categoryDto);
            category.setId(categoryId);
            category.setCreateAt(ct.getCreateAt());
            category.setUpdateAt(LocalDateTime.now().withNano(0));
            Category saved = categoryRepository.save(category);
            return mapToDto(saved);
        }
        return null;
    }

    @Override
    public List<CategoryDto> listOfCategorys() {
        List<Category> categoryList = categoryRepository.findAll();
        return categoryList.stream().map((element) -> mapToDto(element)).collect(Collectors.toList());
    }

    @Override
    public CategoryDto findCategorys(Long categoryId) {
        Optional<Category> opCategory = categoryRepository.findById(categoryId);
        if(opCategory.isPresent()){
            return mapToDto(opCategory.get());
        }
        return null;

    }
}
