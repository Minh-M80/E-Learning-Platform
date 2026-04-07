package com.example.E_Learning_Platform.mapper;

import com.example.E_Learning_Platform.dto.request.CategoryRequest;
import com.example.E_Learning_Platform.dto.request.CourseRequest;
import com.example.E_Learning_Platform.dto.response.CourseResponse;
import com.example.E_Learning_Platform.entity.Category;
import com.example.E_Learning_Platform.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CourseMapper {
    CourseResponse toCourseResponse(Course request);
    Course toCourse(CourseRequest request);

    void updateCourse(@MappingTarget Course course, CourseRequest courseRequest);
}
