package com.example.E_Learning_Platform.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLessonRequest {

    private String title;


    private String videoUrl;


    private String content;


    private Integer orderIndex;



}
