package com.example.E_Learning_Platform.entity;

import com.example.E_Learning_Platform.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    @Column(nullable = false,precision = 12,scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(length = 100)
    private String vnpTransactionNo;

    @Column(length = 255)
    private String vnpOrderInfo;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL,orphanRemoval = true)
    @Builder.Default
    private Set<OrderItem> orderItems = new HashSet<>();
}



