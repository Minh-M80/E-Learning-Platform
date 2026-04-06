package com.example.E_Learning_Platform.mapper;

import com.example.E_Learning_Platform.dto.request.CategoryRequest;
import com.example.E_Learning_Platform.dto.response.CategoryResponse;
import com.example.E_Learning_Platform.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toCategory(CategoryRequest request);

    CategoryResponse toCategoryResponse(Category category);

    void updateCategory(@MappingTarget Category category, CategoryRequest request);
}
