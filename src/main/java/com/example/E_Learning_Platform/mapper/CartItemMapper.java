package com.example.E_Learning_Platform.mapper;

import com.example.E_Learning_Platform.dto.response.CartItemResponse;
import com.example.E_Learning_Platform.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")

public interface CartItemMapper {

    @Mapping(target = "courseId", source = "course.id")
    CartItemResponse toCartItemResponse(CartItem cartItem);


}
