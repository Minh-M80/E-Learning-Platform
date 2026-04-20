package com.example.E_Learning_Platform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private String id;
    private String courseId;
    private String courseTitle;
    private String thumbnail;
    private String instructorName;
    private BigDecimal priceAtPurchase;
}
