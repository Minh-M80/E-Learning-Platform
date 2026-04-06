package com.example.E_Learning_Platform.repository;

import com.example.E_Learning_Platform.entity.Course;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CourseRepository extends JpaRepository<Course,String> {
    Page<Course> findAll(Pageable pageable);
    Page<Course> findByCategoryId(String categoryId,Pageable pageable);

    Page<Course> findByInstructorId(String instructorId, Pageable pageable);

    Page<Course> findByTitleContainingIgnoreCase(String keyword,Pageable pageable);


    Optional<Course> findById(String courseId);

    boolean existsByTitleAndInstructorId(String title,String instructorId);
}
