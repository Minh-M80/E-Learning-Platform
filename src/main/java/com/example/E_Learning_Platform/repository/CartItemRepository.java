package com.example.E_Learning_Platform.repository;

import com.example.E_Learning_Platform.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem,String> {
     List<CartItem> findByCart_Id(String cartId);
     Optional<CartItem> findByCart_IdAndCourse_Id(String cartId, String courseId);
     boolean existsByCart_IdAndCourse_Id(String cartId, String courseId);
     void deleteByCart_IdAndCourse_Id(String cartId, String courseId);
     long countByCart_Id(String cartId);

}
