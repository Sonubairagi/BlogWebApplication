package com.blogapp.controller;

import com.blogapp.payload.CategoryDto;
import com.blogapp.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/category")
public class CategoryController {
//    private CategoryService categoryService;
//    public CategoryController(CategoryService categoryService){
//        this.categoryService = categoryService;
//    }

    @Autowired
    private CategoryService categoryService;

    //http://localhost:8080/api/v1/category/addCategory
    @PostMapping("/addCategory")
    public ResponseEntity<CategoryDto> addCategory(
            @Valid @RequestBody CategoryDto categoryDto
    ){
        CategoryDto category = categoryService.createCategory(categoryDto);
        return new ResponseEntity<>(category, HttpStatus.CREATED);
    }

    //http://localhost:8080/api/v1/category/deleteCategory/{categoryId}
    @DeleteMapping("/deleteCategory/{categoryId}")
    public ResponseEntity<String> deleteCategory(
            @PathVariable Long categoryId
    ){
        String category = categoryService.deleteCategory(categoryId);
        return new ResponseEntity<>(category, HttpStatus.OK);
    }

    //http://localhost:8080/api/v1/category/updateCategory/{categoryId}
    @PutMapping("/updateCategory/{categoryId}")
    public ResponseEntity<CategoryDto> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody CategoryDto categoryDto
    ){
        CategoryDto category = categoryService.updateCategory(categoryId,categoryDto);
        return new ResponseEntity<>(category, HttpStatus.OK);
    }

    //http://localhost:8080/api/v1/category
    @GetMapping
    public ResponseEntity<List<CategoryDto>> getCategorys(){
        List<CategoryDto> categorys = categoryService.listOfCategorys();
        return new ResponseEntity<>(categorys, HttpStatus.OK);
    }

    //http://localhost:8080/api/v1/category/{categoryId}
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDto> findByCategoryId(
            @PathVariable Long categoryId
    ){
        CategoryDto categorys = categoryService.findCategorys(categoryId);
        return new ResponseEntity<>(categorys, HttpStatus.OK);
    }
}
