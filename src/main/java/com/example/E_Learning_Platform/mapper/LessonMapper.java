package com.example.E_Learning_Platform.mapper;

import com.example.E_Learning_Platform.dto.request.LessonRequest;
import com.example.E_Learning_Platform.dto.request.UpdateLessonRequest;
import com.example.E_Learning_Platform.dto.response.LessonResponse;
import com.example.E_Learning_Platform.entity.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")

public interface LessonMapper {
    LessonResponse toLessonResponse(Lesson lesson);

    Lesson toLesson(LessonRequest lessonRequest);

    void updateLesson(@MappingTarget Lesson lesson, UpdateLessonRequest updateLessonRequest);
}
