package com.example.E_Learning_Platform.entity;

import com.example.E_Learning_Platform.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;

@Getter
@Setter
@Table(
        name = "order_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_order_course", columnNames = {"order_id", "course_id"})
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal priceAtPurchase;
}



