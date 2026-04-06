package com.example.E_Learning_Platform.repository;

import com.example.E_Learning_Platform.entity.Category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category,String> {
    boolean existsByName(String name);
    Optional<Category> findByName(String name);
    Page<Category> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

}
