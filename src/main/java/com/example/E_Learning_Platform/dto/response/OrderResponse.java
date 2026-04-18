package com.example.E_Learning_Platform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private String id;
    private String userId;
    private BigDecimal totalPrice;
    private String status;
    private String vnpTransactionNo;
    private String vnpOrderInfo;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}
