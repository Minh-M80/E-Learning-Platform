package com.example.E_Learning_Platform.service;

import com.example.E_Learning_Platform.dto.request.CourseRequest;
import com.example.E_Learning_Platform.dto.request.UpdateCourseRequest;
import com.example.E_Learning_Platform.dto.response.CourseResponse;
import com.example.E_Learning_Platform.entity.Category;
import com.example.E_Learning_Platform.entity.Course;
import com.example.E_Learning_Platform.entity.User;
import com.example.E_Learning_Platform.enums.Role;
import com.example.E_Learning_Platform.exception.AppException;
import com.example.E_Learning_Platform.exception.ErrorCode;
import com.example.E_Learning_Platform.mapper.CourseMapper;
import com.example.E_Learning_Platform.repository.CategoryRepository;
import com.example.E_Learning_Platform.repository.CourseRepository;
import com.example.E_Learning_Platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CourseMapper courseMapper;

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('INSTRUCTOR')")
    @Caching(evict = {
            @CacheEvict(value = "allCourses", allEntries = true),
            @CacheEvict(value = "courseSearch", allEntries = true),
            @CacheEvict(value = "coursesByCategory", allEntries = true),
            @CacheEvict(value = "coursesByInstructor", allEntries = true)
    })
    public CourseResponse createCourse(CourseRequest request) {
        User currentUser = getCurrentUser();
        boolean isAdmin = hasRole(Role.ADMIN.name());
        boolean isInstructor = hasRole(Role.INSTRUCTOR.name());

        String targetInstructorId = request.getInstructorId();

        if (targetInstructorId != null && !targetInstructorId.isBlank()) {
            if (!isAdmin && !targetInstructorId.equals(currentUser.getId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        } else if (isInstructor && !isAdmin) {
            targetInstructorId = currentUser.getId();
        }

        User instructor = userRepository.findById(targetInstructorId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!instructor.getRoles().contains(Role.INSTRUCTOR)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String categoryId = request.getCategoryId();
        if (categoryId == null || categoryId.isBlank()) {
            throw new AppException(ErrorCode.CATEGORY_NOT_EXISTED);
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));

        Course course = courseMapper.toCourse(request);
        course.setInstructor(instructor);
        course.setCategory(category);

        course = courseRepository.save(course);
        return courseMapper.toCourseResponse(course);
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('INSTRUCTOR')")

    @Caching(evict = {
            @CacheEvict(value = "courseById", key = "#courseId"),
            @CacheEvict(value = "allCourses", allEntries = true),
            @CacheEvict(value = "courseSearch", allEntries = true),
            @CacheEvict(value = "coursesByCategory", allEntries = true),
            @CacheEvict(value = "coursesByInstructor", allEntries = true)
    })
    public CourseResponse updateCourse(String courseId, UpdateCourseRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        if (!canManageCourse(course)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        courseMapper.updateCourse(course, request);

        if (request.getCategoryId() != null && !request.getCategoryId().isBlank()) {
            if (!hasRole(Role.ADMIN.name())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));

            course.setCategory(category);
        }

        course = courseRepository.save(course);
        return courseMapper.toCourseResponse(course);
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('INSTRUCTOR')")

    @Caching(evict = {
            @CacheEvict(value = "courseById", key = "#courseId"),
            @CacheEvict(value = "allCourses", allEntries = true),
            @CacheEvict(value = "courseSearch", allEntries = true),
            @CacheEvict(value = "coursesByCategory", allEntries = true),
            @CacheEvict(value = "coursesByInstructor", allEntries = true),
            @CacheEvict(value = "lessonsByCourse", allEntries = true)
    })
    public void deleteCourse(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        if (!canManageCourse(course)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        courseRepository.delete(course);
    }

    @Cacheable(value = "courseById", key = "#courseId")
    public CourseResponse getCourseById(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        return courseMapper.toCourseResponse(course);
    }

    @Cacheable(value = "allCourses", key = "#pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort")
    public Page<CourseResponse> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable)
                .map(courseMapper::toCourseResponse);
    }

    @Cacheable(value = "courseSearch", key = "#keyword + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort")
    public Page<CourseResponse> searchCourses(String keyword, Pageable pageable) {
        return courseRepository.findByTitleContainingIgnoreCase(keyword, pageable)
                .map(courseMapper::toCourseResponse);
    }

    @Cacheable(value = "coursesByCategory", key = "#categoryId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort")
    public Page<CourseResponse> getCoursesByCategory(String categoryId, Pageable pageable) {
        return courseRepository.findByCategory_Id(categoryId, pageable)
                .map(courseMapper::toCourseResponse);
    }


    @Cacheable(value = "coursesByInstructor", key = "#instructorId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort")
    public Page<CourseResponse> getCoursesByInstructor(String instructorId, Pageable pageable) {
        return courseRepository.findByInstructor_Id(instructorId, pageable)
                .map(courseMapper::toCourseResponse);
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('INSTRUCTOR')")
    boolean isCourseOwnedByInstructor(String courseId, String instructorId) {
        if (hasRole(Role.ADMIN.name())) {
            return courseRepository.existsByIdAndInstructor_Id(courseId, instructorId);
        }

        User currentUser = getCurrentUser();

        if (!currentUser.getId().equals(instructorId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return courseRepository.existsByIdAndInstructor_Id(courseId, instructorId);
    }

    private boolean canManageCourse(Course course) {
        if (hasRole(Role.ADMIN.name())) {
            return true;
        }

        User currentUser = getCurrentUser();
        return course.getInstructor() != null
                && course.getInstructor().getId().equals(currentUser.getId());
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities()
                .stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role));
    }
}
