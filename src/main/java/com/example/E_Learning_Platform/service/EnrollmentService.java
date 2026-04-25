package com.example.E_Learning_Platform.service;

import com.example.E_Learning_Platform.dto.response.CourseResponse;
import com.example.E_Learning_Platform.dto.response.UserResponse;
import com.example.E_Learning_Platform.entity.Course;
import com.example.E_Learning_Platform.entity.Enrollment;
import com.example.E_Learning_Platform.entity.Order;
import com.example.E_Learning_Platform.entity.OrderItem;
import com.example.E_Learning_Platform.entity.User;
import com.example.E_Learning_Platform.enums.Role;
import com.example.E_Learning_Platform.exception.AppException;
import com.example.E_Learning_Platform.exception.ErrorCode;
import com.example.E_Learning_Platform.mapper.CourseMapper;
import com.example.E_Learning_Platform.mapper.UserMapper;
import com.example.E_Learning_Platform.repository.CourseRepository;
import com.example.E_Learning_Platform.repository.EnrollmentRepository;
import com.example.E_Learning_Platform.repository.OrderRepository;
import com.example.E_Learning_Platform.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final OrderRepository orderRepository;
    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    public void enrollUserToCourse(String userId, String courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !hasRole(Role.ADMIN.name())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        createEnrollmentIfAbsent(userId, courseId);
    }

    public void enrollUserFromOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOTOUND));

        String userId = order.getUser().getId();
        for (OrderItem item : order.getOrderItems()) {
            createEnrollmentIfAbsent(userId, item.getCourse().getId());
        }
    }

    public List<CourseResponse> getMyEnrolledCourses(String userId) {
        User currentUser = getCurrentUser();
        String targetUserId = resolveTargetUserId(userId, currentUser);

        validateCanViewEnrollments(currentUser, targetUserId);

        return enrollmentRepository.findByUser_Id(targetUserId)
                .stream()
                .map(Enrollment::getCourse)
                .map(courseMapper::toCourseResponse)
                .toList();
    }

    public boolean hasEnrolled(String userId, String courseId) {
        User currentUser = getCurrentUser();
        String targetUserId = resolveTargetUserId(userId, currentUser);

        validateCanViewEnrollments(currentUser, targetUserId);
        return enrollmentRepository.existsByUser_IdAndCourse_Id(targetUserId, courseId);
    }

    public List<UserResponse> getStudentsByCourse(String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        User currentUser = getCurrentUser();
        boolean isAdmin = hasRole(Role.ADMIN.name());
        boolean isOwnerInstructor = hasRole(Role.INSTRUCTOR.name())
                && course.getInstructor() != null
                && course.getInstructor().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwnerInstructor) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return enrollmentRepository.findByCourse_Id(courseId)
                .stream()
                .map(Enrollment::getUser)
                .map(userMapper::toUserResponse)
                .toList();
    }

    private void createEnrollmentIfAbsent(String userId, String courseId) {
        if (enrollmentRepository.existsByUser_IdAndCourse_Id(userId, courseId)) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .enrolledAt(LocalDateTime.now())
                .build();
        enrollmentRepository.save(enrollment);
    }

    private void validateCanViewEnrollments(User currentUser, String targetUserId) {
        if (hasRole(Role.ADMIN.name())) {
            return;
        }

        boolean isSelf = currentUser.getId().equals(targetUserId);
        boolean isStudent = hasRole(Role.STUDENT.name());
        boolean isInstructor = hasRole(Role.INSTRUCTOR.name());

        if (isSelf && (isStudent || isInstructor)) {
            return;
        }

        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private String resolveTargetUserId(String userId, User currentUser) {
        if (userId == null || userId.isBlank()) {
            return currentUser.getId();
        }
        return userId;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities()
                .stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role));
    }
}
