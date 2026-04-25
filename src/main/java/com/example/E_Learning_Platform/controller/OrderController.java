package com.example.E_Learning_Platform.controller;

import com.example.E_Learning_Platform.dto.response.ApiResponse;
import com.example.E_Learning_Platform.dto.response.OrderResponse;
import com.example.E_Learning_Platform.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/from-cart")
    @Operation(summary = "Create order from cart")
    ApiResponse<OrderResponse> createOrderFromCart(@RequestParam(required = false) String userId) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.createOrderFromCart(userId))
                .build();
    }

    @PostMapping("/buy-now/{courseId}")
    @Operation(summary = "Create buy-now order")
    ApiResponse<OrderResponse> createBuyNowOrder(
            @PathVariable String courseId,
            @RequestParam(required = false) String userId) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.createBuyNowOrder(userId, courseId))
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order detail")
    ApiResponse<OrderResponse> getOrderById(@PathVariable("id") String orderId) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.getOrderById(orderId))
                .build();
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Get my orders")
    ApiResponse<List<OrderResponse>> getMyOrders(@RequestParam(required = false) String userId) {
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderService.getMyOrders(userId))
                .build();
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel pending order")
    ApiResponse<Void> cancelOrder(@PathVariable("id") String orderId) {
        orderService.cancelOrder(orderId);
        return ApiResponse.<Void>builder()
                .message("Cancel order successfully")
                .build();
    }
}
