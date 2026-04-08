package com.example.E_Learning_Platform.repository;

import com.example.E_Learning_Platform.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson,String> {
    List<Lesson> findByCourse_IdOrderByOrderIndexAsc(String courseId);


    Optional<Lesson> findById(String lessonId);

    boolean existsByCourse_IdAndOrderIndex(String courseId,Integer orderIndex);

    Long countByCourse_Id(String courseId);
}
