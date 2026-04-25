package com.example.E_Learning_Platform.controller;

import com.example.E_Learning_Platform.dto.response.ApiResponse;
import com.example.E_Learning_Platform.dto.response.CourseResponse;
import com.example.E_Learning_Platform.dto.response.UserResponse;
import com.example.E_Learning_Platform.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/enrollments/my-courses")
    @Operation(summary = "Get enrolled courses")
    ApiResponse<List<CourseResponse>> getMyEnrolledCourses(@RequestParam(required = false) String userId) {
        return ApiResponse.<List<CourseResponse>>builder()
                .result(enrollmentService.getMyEnrolledCourses(userId))
                .build();
    }

    @GetMapping("/enrollments/check/{courseId}")
    @Operation(summary = "Check enrollment status")
    ApiResponse<Boolean> hasEnrolled(
            @PathVariable String courseId,
            @RequestParam(required = false) String userId) {
        return ApiResponse.<Boolean>builder()
                .result(enrollmentService.hasEnrolled(userId, courseId))
                .build();
    }

    @GetMapping("/courses/{courseId}/students")
    @Operation(summary = "Get students by course")
    ApiResponse<List<UserResponse>> getStudentsByCourse(@PathVariable String courseId) {
        return ApiResponse.<List<UserResponse>>builder()
                .result(enrollmentService.getStudentsByCourse(courseId))
                .build();
    }
}
