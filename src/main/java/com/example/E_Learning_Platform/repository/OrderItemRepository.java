package com.example.E_Learning_Platform.repository;


import com.example.E_Learning_Platform.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    List<OrderItem> findByOrder_Id(String orderId);
    boolean existsByOrder_IdAndCourse_Id(String orderId, String courseId);
}
