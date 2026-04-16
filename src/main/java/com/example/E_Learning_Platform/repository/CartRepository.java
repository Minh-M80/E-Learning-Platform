package com.example.E_Learning_Platform.repository;

import com.example.E_Learning_Platform.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart,String> {
    Optional<Cart> findByUser_Id(String userId);
    boolean existsByUser_Id(String userId);
}
