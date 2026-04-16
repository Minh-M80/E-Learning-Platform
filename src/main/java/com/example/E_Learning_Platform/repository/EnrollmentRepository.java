package com.example.E_Learning_Platform.repository;

import com.example.E_Learning_Platform.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment,String> {

     boolean existsByUser_IdAndCourse_Id(String userId, String courseId);
    List<Enrollment> findByUser_Id(String userId);
    List<Enrollment> findByCourse_Id(String courseId);
    Optional<Enrollment> findByUser_IdAndCourse_Id(String userId, String courseId);
    long countByCourse_Id(String courseId);
}
