package com.example.E_Learning_Platform.mapper;


import com.example.E_Learning_Platform.dto.response.OrderItemResponse;
import com.example.E_Learning_Platform.dto.response.OrderResponse;
import com.example.E_Learning_Platform.entity.Order;
import com.example.E_Learning_Platform.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "orderItems", target = "items")
    OrderResponse toOrderResponse(Order order);

    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "course.title", target = "courseTitle")
    @Mapping(source = "course.thumbnail", target = "thumbnail")
    @Mapping(source = "priceAtPurchase", target = "priceAtPurchase")
    @Mapping(target = "instructorName", expression = "java(getInstructorName(item))")
    OrderItemResponse toOrderItemResponse(OrderItem item);

    // MapStruct sẽ tự dùng hàm này cho List
    List<OrderItemResponse> toOrderItemResponses(Set<OrderItem> items);

    // custom logic
    default String getInstructorName(OrderItem item) {
        if (item.getCourse() != null && item.getCourse().getInstructor() != null) {
            return item.getCourse().getInstructor().getUsername();
        }
        return null;
    }
}
