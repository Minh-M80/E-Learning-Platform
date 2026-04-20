package com.example.E_Learning_Platform.service;

import com.example.E_Learning_Platform.dto.response.OrderResponse;
import com.example.E_Learning_Platform.entity.*;
import com.example.E_Learning_Platform.enums.OrderStatus;
import com.example.E_Learning_Platform.enums.Role;
import com.example.E_Learning_Platform.exception.AppException;
import com.example.E_Learning_Platform.exception.ErrorCode;
import com.example.E_Learning_Platform.mapper.OrderMapper;
import com.example.E_Learning_Platform.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final CourseRepository courseRepository;

    public OrderResponse createOrderFromCart(String userId){
        User currentUser= getCurrentUser();

        validateStudentCanOnlyActForSelf(currentUser,userId);

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Cart cart = cartRepository.findByUser_Id(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        List<CartItem> cartItems = cartItemRepository.findByCart_Id(cart.getId());

        if (cartItems.isEmpty()) {
            throw new AppException(ErrorCode.CART_EMPTY);
        }

        Order order = buildOrderFromCourses(
                targetUser,
                cartItems.stream().map(CartItem::getCourse).toList(),
                "Thanh toan don hang tu gio hang"
        );

        return orderMapper.toOrderResponse(orderRepository.save(order));

    }




    public OrderResponse createBuyNowOrder(String userId,String courseId){
        User currentUser = getCurrentUser();

        validateStudentCanOnlyActForSelf(currentUser,userId);

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new AppException(ErrorCode.COURSE_NOT_EXISTED));

        if (enrollmentRepository.existsByUser_IdAndCourse_Id(userId, courseId)) {
            throw new AppException(ErrorCode.ENROLLMENT_EXISTED);
        }

        Order order = buildOrderFromCourses(
                targetUser,
                List.of(course),
                "Thanh toan khoa hoc " + course.getTitle()
        );

        return orderMapper.toOrderResponse(orderRepository.save(order));
    }



    public OrderResponse getOrderById(String orderId) {
        User currentUser = getCurrentUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOTOUND));

        validateCanViewOrder(currentUser, order);

        return orderMapper.toOrderResponse(order);
    }

    public List<OrderResponse> getMyOrders(String userId) {
        User currentUser = getCurrentUser();

        validateStudentCanOnlyActForSelf(currentUser, userId);

        return orderRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }


    public void cancelOrder(String orderId) {
        User currentUser = getCurrentUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOTOUND));

        validateCanCancelOrder(currentUser, order);

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }


    public void markOrderCompleted(String orderId, String vnpTransactionNo) {
        validateAdminOrInternalPayment();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOTOUND));

        if (order.getStatus() == OrderStatus.PAID) {
            return;
        }

        order.setStatus(OrderStatus.PAID);
        order.setVnpTransactionNo(vnpTransactionNo);
        orderRepository.save(order);

        for (OrderItem item : order.getOrderItems()) {
            String userId = order.getUser().getId();
            String courseId = item.getCourse().getId();

            if (!enrollmentRepository.existsByUser_IdAndCourse_Id(userId, courseId)) {
                Enrollment enrollment = Enrollment.builder()
                        .user(order.getUser())
                        .course(item.getCourse())
                        .enrolledAt(LocalDateTime.now())
                        .build();
                enrollmentRepository.save(enrollment);
            }
        }

        clearPurchasedCoursesFromCart(order);
    }


    public void markOrderFailed(String orderId) {
        validateAdminOrInternalPayment();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOTOUND));

        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
        }
    }


    private void clearPurchasedCoursesFromCart(Order order) {
        cartRepository.findByUser_Id(order.getUser().getId()).ifPresent(cart -> {
            for (OrderItem item : order.getOrderItems()) {
                if (cartItemRepository.existsByCart_IdAndCourse_Id(cart.getId(), item.getCourse().getId())) {
                    cartItemRepository.deleteByCart_IdAndCourse_Id(cart.getId(), item.getCourse().getId());
                }
            }
        });
    }





    private Order buildOrderFromCourses(User user, List<Course> courses, String orderInfo) {
        Set<String> uniqueCourseIds = new HashSet<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .vnpOrderInfo(orderInfo)
                .totalPrice(BigDecimal.ZERO)
                .orderItems(new HashSet<>())

                .build();


        for (Course course :courses){
            if (!uniqueCourseIds.add(course.getId())) {
                continue;
            }

            if (enrollmentRepository.existsByUser_IdAndCourse_Id(user.getId(), course.getId())) {
                throw new RuntimeException("Course already purchased: " + course.getTitle());
            }

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .course(course)
                    .priceAtPurchase(course.getPrice())
                    .build();

            order.getOrderItems().add(orderItem);
            totalPrice = totalPrice.add(course.getPrice());
        }

        if (order.getOrderItems().isEmpty()) {
            throw new RuntimeException("No valid course to create order");
        }

        order.setTotalPrice(totalPrice);
        return order;
    }







    private void validateStudentCanOnlyActForSelf(User currentUser, String targetUserId) {
        if (hasRole(Role.ADMIN.name())) {
            return;
        }

        if (!hasRole(Role.STUDENT.name())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (!currentUser.getId().equals(targetUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateCanViewOrder(User currentUser, Order order) {
        if (hasRole(Role.ADMIN.name())) {
            return;
        }

        if (hasRole(Role.STUDENT.name()) && order.getUser().getId().equals(currentUser.getId())) {
            return;
        }

        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private void validateCanCancelOrder(User currentUser, Order order) {
        if (hasRole(Role.ADMIN.name())) {
            return;
        }

        boolean isOwner = order.getUser().getId().equals(currentUser.getId());
        boolean cancelableStatus = order.getStatus() == OrderStatus.PENDING;

        if (hasRole(Role.STUDENT.name()) && isOwner && cancelableStatus) {
            return;
        }

        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private void validateAdminOrInternalPayment() {
        if (hasRole(Role.ADMIN.name())) {
            return;
        }

        if (isInternalPaymentRequest()) {
            return;
        }

        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private boolean isInternalPaymentRequest() {
        // TODO:
        // Sau này nối với payment callback nội bộ:
        // 1. kiểm tra header secret
        // 2. kiểm tra internal API key từ yaml
        // 3. hoặc xác thực service account riêng cho payment
        return false;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities()
                .stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(role));
    }
}
