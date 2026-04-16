package com.example.E_Learning_Platform.service;

import com.example.E_Learning_Platform.dto.request.LessonOrderRequest;
import com.example.E_Learning_Platform.dto.request.LessonRequest;
import com.example.E_Learning_Platform.dto.request.UpdateLessonRequest;
import com.example.E_Learning_Platform.dto.response.LessonResponse;
import com.example.E_Learning_Platform.entity.Course;
import com.example.E_Learning_Platform.entity.Lesson;
import com.example.E_Learning_Platform.entity.User;
import com.example.E_Learning_Platform.enums.Role;
import com.example.E_Learning_Platform.exception.AppException;
import com.example.E_Learning_Platform.exception.ErrorCode;
import com.example.E_Learning_Platform.mapper.LessonMapper;
import com.example.E_Learning_Platform.repository.CourseRepository;
import com.example.E_Learning_Platform.repository.EnrollmentRepository;
import com.example.E_Learning_Platform.repository.LessonRepository;
import com.example.E_Learning_Platform.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LessonService {
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonMapper lessonMapper;
    private final EnrollmentRepository enrollmentRepository;


    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('INSTRUCTOR')")
    public LessonResponse createLesson(LessonRequest request){
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(
                        () -> new AppException(ErrorCode.COURSE_NOT_EXISTED)
                );

        if(!canManageCourse(course)){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Integer orderIndex = request.getOrderIndex();
        if(orderIndex == null){
            orderIndex = lessonRepository.countByCourse_Id(course.getId()).intValue() +1;
        }
          if (orderIndex <= 0) {
            throw new IllegalArgumentException("orderIndex must be greater than 0");
        }

        if(lessonRepository.existsByCourse_IdAndOrderIndex(course.getId(),orderIndex)){
            throw new AppException(ErrorCode.LESSON_ORDER_DUPLICATED);
        }

        Lesson lesson = lessonMapper.toLesson(request);
        lesson.setCourse(course);
        lesson.setOrderIndex(orderIndex);

        lessonRepository.save(lesson);

        return lessonMapper.toLessonResponse(lesson);

    }


    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('INSTRUCTOR')")
    public LessonResponse updateLesson(String lessonId, UpdateLessonRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_EXISTED)
                );

        Course course = lesson.getCourse();

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            lesson.setTitle(request.getTitle());
        }

        if (request.getVideoUrl() != null) {
            lesson.setVideoUrl(request.getVideoUrl());
        }

        if (request.getContent() != null) {
            lesson.setContent(request.getContent());
        }

        if(request.getOrderIndex() != null){
            if(request.getOrderIndex() <= 0){
                throw new IllegalArgumentException("orderIndex must be greater than 0");
            }
            boolean duplicated = lessonRepository.existsByCourse_IdAndOrderIndex(course.getId(), request.getOrderIndex());
            if (duplicated && !request.getOrderIndex().equals(lesson.getOrderIndex())) {
                throw new AppException(ErrorCode.LESSON_ORDER_DUPLICATED);
            }

            lesson.setOrderIndex(request.getOrderIndex());
        }

        lessonRepository.save(lesson);
        return lessonMapper.toLessonResponse(lesson);


    }


    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('INSTRUCTOR')")
    public void deleteLesson(String lessonId){
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(
                        () -> new AppException(ErrorCode.LESSON_NOT_EXISTED)
                );

        if(!canManageCourse(lesson.getCourse())){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        lessonRepository.delete(lesson);
    }



    public LessonResponse getLessonById(String lessonId){
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(
                        () -> new AppException(ErrorCode.LESSON_NOT_EXISTED)
                );

        Course course = lesson.getCourse();
        User currentUsser = getCurrentUserOrNull();

        if(canManageCourse(course)){
            return lessonMapper.toLessonResponse(lesson);
        }

        if(currentUsser == null){
            throw  new AppException(ErrorCode.UNAUTHORIZED);
        }

        boolean enrolled = enrollmentRepository.existsByUser_IdAndCourse_Id(currentUsser.getId(),course.getId());

        if(!enrolled){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Không public lesson chưa có nội dung học
        if ((lesson.getContent() == null || lesson.getContent().isBlank())
                && (lesson.getVideoUrl() == null || lesson.getVideoUrl().isBlank())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return lessonMapper.toLessonResponse(lesson);


    }


    public List<LessonResponse> getLessonsByCourse(String courseId){
        Course course = courseRepository.findById(courseId)
                .orElseThrow(
                        () -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED)
                );

        if(canManageCourse(course)){
            return lessonRepository.findByCourse_IdOrderByOrderIndexAsc(courseId)
                    .stream()
                    .map(lessonMapper::toLessonResponse)
                    .toList();
        }

        User currentUser = getCurrentUserOrNull();
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        boolean enrolled = enrollmentRepository.existsByUser_IdAndCourse_Id(currentUser.getId(), courseId);
        if (!enrolled) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return lessonRepository.findByCourse_IdOrderByOrderIndexAsc(courseId)
                .stream()
                .filter(this::hasLearningContent)
                .map(lessonMapper::toLessonResponse)
                .toList();
    }


    @Transactional
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('INSTRUCTOR')")
    public void reorderLessons(String courseId, LessonOrderRequest request){
        Course course = courseRepository.findById(courseId)
                .orElseThrow(
                        () -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED)
                );

        if(!canManageCourse(course)){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if(request.getLessonId() == null || request.getLessonId().isBlank()){
            throw new IllegalArgumentException("lessonId must not be blank");
        }

        if(request.getNewOrderIndex() == null || request.getNewOrderIndex() <= 0){
            throw new IllegalArgumentException("newOrderIndex must be greater than 0");
        }


        List<Lesson> lessons = lessonRepository.findByCourse_IdOrderByOrderIndexAsc(courseId);

        Lesson targetLesson = lessons.stream()
                .filter(lesson -> lesson.getId().equals(request.getLessonId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Lesson does not belong to this course"));

        int oldIndex = targetLesson.getOrderIndex();
        int newIndex = request.getNewOrderIndex();

        if(oldIndex == newIndex){
            return;
        }

        if(newIndex < oldIndex){
            for (Lesson lesson : lessons){
                if(!lesson.getId().equals(targetLesson.getId())
                        && lesson.getOrderIndex() >= newIndex
                        && lesson.getOrderIndex() < oldIndex
                ){
                    lesson.setOrderIndex(lesson.getOrderIndex() + 1);
                }

            }
        } else {
            for (Lesson lesson : lessons) {
                if (!lesson.getId().equals(targetLesson.getId())
                        && lesson.getOrderIndex() <= newIndex
                        && lesson.getOrderIndex() > oldIndex) {
                    lesson.setOrderIndex(lesson.getOrderIndex() - 1);
                }
            }
        }


        targetLesson.setOrderIndex(newIndex);

        lessonRepository.saveAll(lessons);




    }







    private boolean canManageCourse(Course course){
        if(hasRole(Role.ADMIN.name())){
            return true;
        }
        if(!hasRole(Role.INSTRUCTOR.name())){
            return false;
        }

        User currentUser = getCurrentUser();

        return course.getInstructor() != null
                && course.getInstructor().getId().equals(currentUser.getId());
    }

    private boolean hasLearningContent(Lesson lesson){
        return (lesson.getContent() != null && !lesson.getContent().isBlank())
                || (lesson.getVideoUrl() != null && !lesson.getVideoUrl().isBlank());
    }

    private User getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED)
        );

    }

    private User getCurrentUserOrNull(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || authentication.getName() ==null){
            return null;
        }

        return userRepository.findByEmail(authentication.getName()).orElse(null);
    }

    private boolean hasRole(String role){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null){
            return false;
        }
        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }


}
