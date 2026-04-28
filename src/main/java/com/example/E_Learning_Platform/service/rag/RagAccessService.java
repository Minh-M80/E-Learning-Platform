// sample: service/rag/RagAccessService.java
package com.example.E_Learning_Platform.service.rag;


import com.example.E_Learning_Platform.entity.User;
import com.example.E_Learning_Platform.exception.AppException;
import com.example.E_Learning_Platform.exception.ErrorCode;
import com.example.E_Learning_Platform.repository.CourseRepository;
import com.example.E_Learning_Platform.repository.EnrollmentRepository;
import com.example.E_Learning_Platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RagAccessService {
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    public RagScope buildScopeByEmail(String email) {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Set<String> enrolled = enrollmentRepository.findByUser_Id(u.getId())
                .stream().map(e -> e.getCourse().getId()).collect(Collectors.toSet());

        Set<String> ownCourses = courseRepository.findByInstructor_Id(u.getId(),
                        org.springframework.data.domain.PageRequest.of(0, 10000))
                .stream().map(c -> c.getId()).collect(Collectors.toSet());

        return new RagScope(u.getId(), u.getRoles(), enrolled, ownCourses);
    }

    public boolean canRead(Document d, RagScope s) {
        if (s.isAdmin()) return true;
        Map<String, Object> m = d.getMetadata();

        String type = str(m.get("entityType"));
        String access = str(m.get("access")); // PUBLIC, ENROLLED, OWNER, SELF
        String courseId = str(m.get("courseId"));
        String ownerId = str(m.get("ownerUserId"));
        String instructorId = str(m.get("instructorId"));

        if ("PUBLIC".equals(access)) return true;
        if ("SELF".equals(access)) return s.userId().equals(ownerId);
        if ("OWNER".equals(access)) return s.userId().equals(instructorId);
        if ("ENROLLED".equals(access)) {
            return s.enrolledCourseIds().contains(courseId) || s.instructorCourseIds().contains(courseId);
        }

        // fallback theo type
        if ("user".equals(type)) return s.userId().equals(ownerId);
        if ("order".equals(type)) return s.userId().equals(ownerId);
        if ("enrollment".equals(type)) return s.userId().equals(ownerId) || s.instructorCourseIds().contains(courseId);

        return false;
    }

    private String str(Object o) { return o == null ? "" : String.valueOf(o); }
}
