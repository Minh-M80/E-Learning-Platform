package com.example.E_Learning_Platform.controller;

import com.example.E_Learning_Platform.dto.request.CourseRequest;
import com.example.E_Learning_Platform.dto.response.ApiResponse;
import com.example.E_Learning_Platform.dto.response.CourseResponse;
import com.example.E_Learning_Platform.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @PostMapping
    @Operation(
            summary = "Create course",
            description = "Create a new course"
    )
    ApiResponse<CourseResponse> createCourse(@RequestBody CourseRequest request) {
        log.info("Controller:createCourse");
        return ApiResponse.<CourseResponse>builder()
                .result(courseService.createCourse(request))
                .message("Create successful course")
                .build();
    }

    @PutMapping("/{courseId}")
    @Operation(
            summary = "Update course",
            description = "Update course information"
    )
    ApiResponse<CourseResponse> updateCourse(
            @PathVariable String courseId,
            @RequestBody CourseRequest request) {
        log.info("Controller:updateCourse");
        return ApiResponse.<CourseResponse>builder()
                .result(courseService.updateCourse(courseId, request))
                .message("Update successful course")
                .build();
    }

    @DeleteMapping("/{courseId}")
    @Operation(
            summary = "Delete course",
            description = "Delete a course by id"
    )
    ApiResponse<Void> deleteCourse(@PathVariable String courseId) {
        log.info("Controller:deleteCourse");
        courseService.deleteCourse(courseId);
        return ApiResponse.<Void>builder()
                .message("Delete successful course")
                .build();
    }

    @GetMapping("/{courseId}")
    @Operation(
            summary = "Get course by id",
            description = "Get course details by id"
    )
    ApiResponse<CourseResponse> getCourseById(@PathVariable String courseId) {
        log.info("Controller:getCourseById");
        return ApiResponse.<CourseResponse>builder()
                .result(courseService.getCourseById(courseId))
                .message("Get course successful")
                .build();
    }

    @GetMapping
    @Operation(
            summary = "Get all courses",
            description = "Get all courses with pagination"
    )
    ApiResponse<Page<CourseResponse>> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        log.info("Controller:getAllCourses");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return ApiResponse.<Page<CourseResponse>>builder()
                .result(courseService.getAllCourses(pageable))
                .message("Get all courses successful")
                .build();
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search courses",
            description = "Search courses by keyword"
    )
    ApiResponse<Page<CourseResponse>> searchCourses(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        log.info("Controller:searchCourses");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return ApiResponse.<Page<CourseResponse>>builder()
                .result(courseService.searchCourses(keyword, pageable))
                .message("Search courses successful")
                .build();
    }

    @GetMapping("/category/{categoryId}")
    @Operation(
            summary = "Get courses by category",
            description = "Get all courses by category id"
    )
    ApiResponse<Page<CourseResponse>> getCoursesByCategory(
            @PathVariable String categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        log.info("Controller:getCoursesByCategory");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return ApiResponse.<Page<CourseResponse>>builder()
                .result(courseService.getCoursesByCategory(categoryId, pageable))
                .message("Get courses by category successful")
                .build();
    }

    @GetMapping("/instructor/{instructorId}")
    @Operation(
            summary = "Get courses by instructor",
            description = "Get all courses by instructor id"
    )
    ApiResponse<Page<CourseResponse>> getCoursesByInstructor(
            @PathVariable String instructorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        log.info("Controller:getCoursesByInstructor");
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return ApiResponse.<Page<CourseResponse>>builder()
                .result(courseService.getCoursesByInstructor(instructorId, pageable))
                .message("Get courses by instructor successful")
                .build();
    }
}
