package com.example.E_Learning_Platform.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCourseRequest {
    private String title;


    private String description;


    private BigDecimal price;


    private String thumbnail;


    private String categoryId;


}
