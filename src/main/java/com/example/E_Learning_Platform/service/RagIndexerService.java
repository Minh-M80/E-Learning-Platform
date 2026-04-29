// sample: service/RagIndexerService.java
package com.example.E_Learning_Platform.service;

import com.example.E_Learning_Platform.entity.*;
import com.example.E_Learning_Platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RagIndexerService {
    private final VectorStore vectorStore;
    private final CourseRepository courseRepo;
    private final LessonRepository lessonRepo;
    private final UserRepository userRepo;
    private final OrderRepository orderRepo;
    private final EnrollmentRepository enrollmentRepo;

    public void reindexAll() {
        List<Document> docs = new ArrayList<>();
        TokenTextSplitter splitter = new TokenTextSplitter(300, 120, 5, 200, true);

        for (Course c : courseRepo.findAll()) {
            Map<String, Object> metadata = Map.of(
                    "entityType", "course",
                    "entityId", c.getId(),
                    "courseId", c.getId(),
                    "instructorId", c.getInstructor().getId(),
                    "access", "PUBLIC"
            );

            docs.add(new Document(
                    """
                    This is an online course.

                    Course Title: %s
                    Category: %s
                    Description: %s
                    Price: %s

                    This course is created by instructor with ID: %s.
                    Students can enroll to access full content.
                    """.formatted(
                            safe(c.getTitle()),
                            c.getCategory() == null ? "" : safe(c.getCategory().getName()),
                            safe(c.getDescription()),
                            c.getPrice(),
                            c.getInstructor() == null ? "" : c.getInstructor().getId()
                    ),
                    metadata));
        }

        for (Lesson l : lessonRepo.findAll()) {
            boolean isPreview = l.getOrderIndex() != null && l.getOrderIndex() <= 2;
            String content = """
                    Lesson Title: %s

                    This lesson belongs to course: %s

                    Content:
                    %s
                    """.formatted(
                    safe(l.getTitle()),
                    l.getCourse() == null ? "" : safe(l.getCourse().getTitle()),
                    safe(l.getContent())
            );

            Map<String, Object> baseMetadata = Map.of(
                    "entityType", "lesson",
                    "entityId", l.getId(),
                    "courseId", l.getCourse().getId(),
                    "instructorId", l.getCourse().getInstructor().getId(),
                    "access", isPreview ? "PUBLIC" : "ENROLLED"
            );

            List<Document> chunkDocs = splitter.split(new Document(content, baseMetadata));
            for (int i = 0; i < chunkDocs.size(); i++) {
                Map<String, Object> metadata = new HashMap<>();
                metadata.putAll(chunkDocs.get(i).getMetadata());
                metadata.put("chunkIndex", i);
                metadata.put("chunkCount", chunkDocs.size());

                docs.add(new Document(chunkDocs.get(i).getText(), metadata));
            }
        }

        for (User u : userRepo.findAll()) {
            docs.add(new Document(
                    "User profile: username=" + u.getUsername() + ", email=" + u.getEmail() +
                            ", roles=" + u.getRoles(),
                    Map.of(
                            "entityType", "user",
                            "entityId", u.getId(),
                            "ownerUserId", u.getId(),
                            "access", "SELF"
                    )));
        }

        for (Order o : orderRepo.findAll()) {
            docs.add(new Document(
                    "Order: id=" + o.getId() + ", status=" + o.getStatus() +
                            ", total=" + o.getTotalPrice() + ", createdAt=" + o.getCreatedAt(),
                    Map.of(
                            "entityType", "order",
                            "entityId", o.getId(),
                            "ownerUserId", o.getUser().getId(),
                            "access", "SELF"
                    )));
        }

        for (Enrollment e : enrollmentRepo.findAll()) {
            docs.add(new Document(
                    "Enrollment: userId=" + e.getUser().getId() + ", courseId=" + e.getCourse().getId() +
                            ", enrolledAt=" + e.getEnrolledAt(),
                    Map.of(
                            "entityType", "enrollment",
                            "entityId", e.getId(),
                            "ownerUserId", e.getUser().getId(),
                            "courseId", e.getCourse().getId(),
                            "instructorId", e.getCourse().getInstructor().getId(),
                            "access", "ENROLLED"
                    )));
        }

        vectorStore.add(docs);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
