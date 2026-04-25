package com.example.E_Learning_Platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {
    private String id;
    private String title;


    private String description;


    private BigDecimal price;


    private String thumbnail;


    private CategoryResponse  category;


    private UserResponse instructor;

    private Long totalLessons;
    private Long totalStudents;

    private boolean enrolled;

    private boolean inCart;

}
