package com.example.E_Learning_Platform.mapper;

import com.example.E_Learning_Platform.dto.request.CourseRequest;
import com.example.E_Learning_Platform.dto.request.UpdateCourseRequest;
import com.example.E_Learning_Platform.dto.response.CourseResponse;
import com.example.E_Learning_Platform.entity.Course;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CourseMapper {
    CourseResponse toCourseResponse(Course request);
    Course toCourse(CourseRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCourse(@MappingTarget Course course, UpdateCourseRequest courseRequest);
}
