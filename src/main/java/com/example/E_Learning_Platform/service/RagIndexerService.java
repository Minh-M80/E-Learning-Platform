// sample: service/RagIndexerService.java
package com.example.E_Learning_Platform.service;

import com.example.E_Learning_Platform.entity.*;
import com.example.E_Learning_Platform.enums.OrderStatus;
import com.example.E_Learning_Platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

        for (Course c : courseRepo.findAll()) {
            docs.add(new Document(
                    "Course: " + c.getTitle() + "\nCategory: " + c.getCategory().getName() +
                            "\nDescription: " + c.getDescription() + "\nPrice: " + c.getPrice(),
                    Map.of(
                            "entityType", "course",
                            "entityId", c.getId(),
                            "courseId", c.getId(),
                            "instructorId", c.getInstructor().getId(),
                            "access", "PUBLIC"
                    )));
        }

        for (Lesson l : lessonRepo.findAll()) {
            boolean isPreview = l.getOrderIndex() != null && l.getOrderIndex() <= 2;
            docs.add(new Document(
                    "Lesson: " + l.getTitle() + "\nContent: " + l.getContent(),
                    Map.of(
                            "entityType", "lesson",
                            "entityId", l.getId(),
                            "courseId", l.getCourse().getId(),
                            "instructorId", l.getCourse().getInstructor().getId(),
                            "access", isPreview ? "PUBLIC" : "ENROLLED"
                    )));
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
}
