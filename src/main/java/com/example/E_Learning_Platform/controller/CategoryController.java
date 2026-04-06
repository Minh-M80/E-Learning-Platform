package com.example.E_Learning_Platform.controller;

import com.example.E_Learning_Platform.dto.request.CategoryRequest;
import com.example.E_Learning_Platform.dto.response.ApiResponse;
import com.example.E_Learning_Platform.dto.response.CategoryResponse;
import com.example.E_Learning_Platform.repository.CategoryRepository;
import com.example.E_Learning_Platform.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;

    @Operation(
            summary = "Create category",
            description = "Create category"
    )
    @PostMapping()
    ApiResponse<CategoryResponse> create(@RequestBody CategoryRequest request){
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.createCategory(request))
                .message("Create successfull category")

                .build();
    }

    @Operation(
            summary = "Update Category",
            description = "Update Category"
    )
    @PutMapping("/{id}")
    ApiResponse<CategoryResponse> update(
            @PathVariable String id,
            @RequestBody CategoryRequest request){
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.updateCategory(id, request))
                .message("Update successfull category")
                .build();
    }

    @Operation(
            summary = "Search categories",
            description = "Search categories by keyword"
    )
    @GetMapping("/search")
    ApiResponse<Page<CategoryResponse>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        return ApiResponse.<Page<CategoryResponse>>builder()
                .result(categoryService.searchCategories(keyword, pageable))
                .message("Search categories successful")
                .build();
    }


    @GetMapping
    ApiResponse<List<CategoryResponse>> getAllCategory(){
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(categoryService.getAllCategories())
                .message("get all category")
                .build();
    }


    @GetMapping("{id}")
    ApiResponse<CategoryResponse> getCategoryById(
            @PathVariable String id
    ){
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.getCategoryById(id))
                .message("get category by id: " + id)
                .build();
    }





    @DeleteMapping("{id}")
    ApiResponse<Void> delete(
            @PathVariable String id
    ){

        categoryRepository.deleteById(id);

        return ApiResponse.<Void>builder()
                .message("delete successfull")
                .build();

    }
}
