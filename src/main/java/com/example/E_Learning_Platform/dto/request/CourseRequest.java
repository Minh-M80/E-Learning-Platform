package com.example.E_Learning_Platform.dto.request;

import com.example.E_Learning_Platform.entity.Category;
import com.example.E_Learning_Platform.entity.Lesson;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseRequest {

    private String title;


    private String description;


    private BigDecimal price;


    private String thumbnail;


    private String categoryId;

    private String instructorId;

}
