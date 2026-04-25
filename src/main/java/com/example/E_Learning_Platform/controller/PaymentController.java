package com.example.E_Learning_Platform.controller;

import com.example.E_Learning_Platform.dto.response.ApiResponse;
import com.example.E_Learning_Platform.dto.response.PaymentCallbackResponse;
import com.example.E_Learning_Platform.dto.response.PaymentUrlResponse;
import com.example.E_Learning_Platform.service.VnPayService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final VnPayService vnPayService;

    @PostMapping("/vnpay/{orderId}")
    @Operation(summary = "Create VNPay payment URL")
    ApiResponse<PaymentUrlResponse> createPaymentUrl(@PathVariable String orderId) {
        return ApiResponse.<PaymentUrlResponse>builder()
                .result(PaymentUrlResponse.builder()
                        .orderId(orderId)
                        .paymentUrl(vnPayService.createPaymentUrl(orderId))
                        .build())
                .build();
    }

    @GetMapping("/vnpay-return")
    @Operation(summary = "Handle VNPay return callback", security = {})
    ApiResponse<PaymentCallbackResponse> handleVnPayReturn(@RequestParam Map<String, String> params) {
        return ApiResponse.<PaymentCallbackResponse>builder()
                .result(vnPayService.handleVnPayReturn(params))
                .build();
    }
}
