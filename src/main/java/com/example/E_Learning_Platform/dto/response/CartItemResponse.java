package com.example.E_Learning_Platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private String id;

    private String courseId;
    private String courseTitle;
    private BigDecimal coursePrice;
    private String thumbnailUrl;

    private LocalDateTime addedAt;
}
