package com.example.E_Learning_Platform.service;

import com.example.E_Learning_Platform.dto.response.CartItemResponse;
import com.example.E_Learning_Platform.dto.response.CartResponse;
import com.example.E_Learning_Platform.entity.Cart;
import com.example.E_Learning_Platform.entity.CartItem;
import com.example.E_Learning_Platform.entity.Course;
import com.example.E_Learning_Platform.entity.User;
import com.example.E_Learning_Platform.exception.AppException;
import com.example.E_Learning_Platform.exception.ErrorCode;
import com.example.E_Learning_Platform.mapper.CartMapper;
import com.example.E_Learning_Platform.repository.CartRepository;
import com.example.E_Learning_Platform.repository.CourseRepository;
import com.example.E_Learning_Platform.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CartItemService cartItemService;
    private final CartMapper cartMapper;

    public CartResponse getMyCart(){
        User currentUser = getCurrentUser();
        Cart cart = createOrGetCart(currentUser);
        return buildCartResponse(cart);
    }


    @Transactional
    public CartResponse addCourseToCart(String courseId){
        User user = getCurrentUser();
        Cart cart = createOrGetCart(user);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(
                        () -> new AppException(ErrorCode.COURSE_NOT_EXISTED)
                );

        cartItemService.addCourseToCart(cart,course);

        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse removeCourseFromCart(String courseId){
        User user = getCurrentUser();
        Cart cart = createOrGetCart(user);

        cartItemService.removeCourseFromCart(cart.getId(),courseId);

        return buildCartResponse(cart);
    }


    @Transactional
    public void clearMyCart() {
        User currentUser = getCurrentUser();
        Cart cart = createOrGetCart(currentUser);
        cartItemService.clearCart(cart.getId());
    }

    public boolean isCourseInCart(String userId, String courseId) {
        return cartRepository.findByUser_Id(userId)
                .map(cart -> cartItemService.isCourseInCart(cart.getId(), courseId))
                .orElse(false);
    }


    @Transactional
    public CartResponse createCartIfNotExists(String userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Cart cart = createOrGetCart(user);
        return buildCartResponse(cart);
    }




    private Cart createOrGetCart(User user){
        return cartRepository.findByUser_Id(user.getId())
                .orElseGet(
                        () -> cartRepository.save(
                                Cart.builder()
                                        .user(user)
                                        .createdAt(LocalDateTime.now())
                                        .build()
                        )
                );
    }


    private CartResponse buildCartResponse(Cart cart){
        List<CartItemResponse> items = cartItemService.getCartItemResponses(cart.getId());

        CartResponse response = cartMapper.toCartResponse(cart);

        response.setUserId(cart.getUser().getId());
        response.setItems(items);
        response.setTotalItems(items.size());

        return response;

    }



    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new AppException(ErrorCode.USER_NOT_EXISTED)
                );
    }
}
