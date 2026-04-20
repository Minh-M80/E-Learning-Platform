package com.example.E_Learning_Platform.repository;

import com.example.E_Learning_Platform.entity.Order;
import com.example.E_Learning_Platform.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUser_IdOrderByCreatedAtDesc(String userId);
    Optional<Order> findByVnpTransactionNo(String vnpTransactionNo);
    List<Order> findByStatus(OrderStatus status);
    boolean existsById(String orderId);
}
