package com.example.E_Learning_Platform.service;

import com.example.E_Learning_Platform.dto.request.CategoryRequest;
import com.example.E_Learning_Platform.dto.response.CategoryResponse;
import com.example.E_Learning_Platform.entity.Category;
import com.example.E_Learning_Platform.exception.AppException;
import com.example.E_Learning_Platform.exception.ErrorCode;
import com.example.E_Learning_Platform.mapper.CategoryMapper;
import com.example.E_Learning_Platform.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('INSTRUCTOR')")
     public CategoryResponse createCategory(CategoryRequest request){

        Category category = categoryMapper.toCategory(request);
        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('INSTRUCTOR')")
    public CategoryResponse updateCategory(String categoryId, CategoryRequest request){
         Category category = categoryRepository.findById(categoryId).orElseThrow(
                 () -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED)
         );
         category.setName(request.getName());
         category.setDescription(request.getDescription());
         categoryRepository.save(category);
         return categoryMapper.toCategoryResponse(category);

    }

    public CategoryResponse getCategoryById(String categoryId){
         Category category = categoryRepository.findById(categoryId).orElseThrow(
                 () -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED)
         );
         return categoryMapper.toCategoryResponse(category);
    }


   public List<CategoryResponse> getAllCategories(){
         return categoryRepository.findAll()
                 .stream()
                 .map(categoryMapper::toCategoryResponse).toList();
   }

   public Page<CategoryResponse> searchCategories(String keyword, Pageable pageable){
        log.info("Search categories with keyword: {}", keyword);
        Page<Category> categories = categoryRepository.findByNameContainingIgnoreCase(keyword, pageable);
        return categories.map(categoryMapper::toCategoryResponse);
   }



    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('INSTRUCTOR')")
   public void deleteCategory(String categoryId){
          categoryRepository.deleteById(categoryId);
   }


}
