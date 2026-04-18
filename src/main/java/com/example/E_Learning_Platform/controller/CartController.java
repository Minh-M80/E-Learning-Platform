package com.example.E_Learning_Platform.controller;

import com.example.E_Learning_Platform.dto.response.ApiResponse;
import com.example.E_Learning_Platform.dto.response.CartResponse;
import com.example.E_Learning_Platform.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(
            summary = "Get cart",
            security = {}
    )
    ApiResponse<CartResponse> getMyCart(){
        return ApiResponse.<CartResponse>builder()
                .result(cartService.getMyCart())
                .build();
    }

    @PostMapping("/items/{courseId}")
    @Operation(
            summary = "Add course to cart",
            security = {}
    )
    ApiResponse<CartResponse> addCourseToCart(
            @PathVariable String courseId
    ){
        return ApiResponse.<CartResponse>builder()
                .result(cartService.addCourseToCart(courseId))
                .build();
    }

    @DeleteMapping("/items/{courseId}")
    @Operation(
            summary = "remove course from cart",
            security = {}
    )
    ApiResponse<CartResponse> removeCourseFromCart(
            @PathVariable String courseId
    ){
        return ApiResponse.<CartResponse>builder()
                .result(cartService.removeCourseFromCart(courseId))
                .build();
    }

    @DeleteMapping("/clear")
    @Operation(
            summary = "Clear cart",
            security = {}
    )
    ApiResponse<Void> clearCart(

    ){
        cartService.clearMyCart();
        return ApiResponse.<Void>builder()
                .message("Clear cart successfully")
                .build();
    }


    @GetMapping("/items/{courseId}/exists")

    @Operation(summary = "Check course is in my cart")
    public ApiResponse<Boolean> isMyCourseInCart(@PathVariable String courseId) {
        return ApiResponse.<Boolean>builder()
                .result(cartService.isMyCourseInCart(courseId))
                .build();
    }


}
