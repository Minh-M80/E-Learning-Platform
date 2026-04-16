package com.example.E_Learning_Platform.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(
        name = "lesson",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"course_id", "order_index"})
        }
)
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String videoUrl;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}
