package com.example.E_Learning_Platform.controller;

import com.example.E_Learning_Platform.dto.request.LessonOrderRequest;
import com.example.E_Learning_Platform.dto.request.LessonRequest;
import com.example.E_Learning_Platform.dto.request.UpdateLessonRequest;
import com.example.E_Learning_Platform.dto.response.ApiResponse;
import com.example.E_Learning_Platform.dto.response.LessonResponse;
import com.example.E_Learning_Platform.repository.LessonRepository;
import com.example.E_Learning_Platform.service.LessonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController

@RequiredArgsConstructor
public class LessonController {
    private final LessonService lessonService;
    private final LessonRepository lessonRepository;

    @PostMapping("/lessons")
    ApiResponse<LessonResponse> createLesson(
            @RequestBody LessonRequest lessonRequest
            ){
        return ApiResponse.<LessonResponse>builder()
                .result(lessonService.createLesson(lessonRequest))
                .build();
    }

    @PutMapping("/lessons/{id}")
    ApiResponse<LessonResponse> updateLesson(
            @RequestBody UpdateLessonRequest request,
            @PathVariable String id
    ){
        return ApiResponse.<LessonResponse>builder()
                .result(lessonService.updateLesson(id,request))
                .build();
    }

    @DeleteMapping("/lessons/{id}")
    ApiResponse<Void> deleteLesson(@PathVariable String id){
        lessonRepository.deleteById(id);
        return ApiResponse.<Void>builder()
                .message("delete successfully")
                .build();
    }

    @GetMapping("/lessons/{id}")
    ApiResponse<LessonResponse> getLessonById(
            @PathVariable String id
    ){
        return ApiResponse.<LessonResponse>builder()
                .result(lessonService.getLessonById(id))
                .build();
    }

    @GetMapping("/courses/{courseId}/lessons")
    ApiResponse<List<LessonResponse>> getLessonsByCourse(
            @PathVariable String courseId
    ){
        return ApiResponse.<List<LessonResponse>>builder()
                .result(lessonService.getLessonsByCourse(courseId))
                .build();
    }

    @PutMapping("/courses/{courseId}/lessons/reorder")
    ApiResponse<Void> reorderLessons(
            @PathVariable String courseId,
            @RequestBody LessonOrderRequest request
    ){
        lessonService.reorderLessons(courseId,request);
        return  ApiResponse.<Void>builder()
                .message("reorder successfully")
                .build();
    }










}
