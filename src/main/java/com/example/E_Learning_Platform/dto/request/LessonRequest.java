package com.example.E_Learning_Platform.dto.request;

import com.example.E_Learning_Platform.entity.Course;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonRequest {

    private String title;


    private String videoUrl;


    private String content;


    private Integer orderIndex;


    private String courseId;
}
