package com.example.E_Learning_Platform.repository;

import com.example.E_Learning_Platform.entity.Course;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course,String> {
    Page<Course> findAll(Pageable pageable);
    Page<Course> findByCategory_Id(String categoryId,Pageable pageable);

    Page<Course> findByInstructor_Id(String instructorId, Pageable pageable);

    Page<Course> findByTitleContainingIgnoreCase(String keyword,Pageable pageable);


    Optional<Course> findById(String courseId);

    boolean existsByTitleAndInstructor_Id(String title,String instructorId);

    boolean existsByIdAndInstructor_Id(String courseId, String instructorId);
}
