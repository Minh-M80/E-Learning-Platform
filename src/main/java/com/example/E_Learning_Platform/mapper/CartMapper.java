package com.example.E_Learning_Platform.mapper;

import com.example.E_Learning_Platform.dto.response.CartResponse;
import com.example.E_Learning_Platform.entity.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")

public interface CartMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "totalItems", ignore = true)
    CartResponse toCartResponse(Cart cart);
}
