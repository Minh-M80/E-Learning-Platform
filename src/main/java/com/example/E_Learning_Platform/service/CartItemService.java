package com.example.E_Learning_Platform.service;


import com.example.E_Learning_Platform.dto.response.CartItemResponse;
import com.example.E_Learning_Platform.entity.Cart;
import com.example.E_Learning_Platform.entity.CartItem;
import com.example.E_Learning_Platform.entity.Course;
import com.example.E_Learning_Platform.entity.User;
import com.example.E_Learning_Platform.exception.AppException;
import com.example.E_Learning_Platform.exception.ErrorCode;
import com.example.E_Learning_Platform.mapper.CartItemMapper;
import com.example.E_Learning_Platform.mapper.CartMapper;
import com.example.E_Learning_Platform.repository.CartItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;

    public List<CartItem> getCartItems(String cardId){
        return cartItemRepository.findByCart_Id(cardId);

    }

    public List<CartItemResponse> getCartItemResponses(String cartId){
        return cartItemRepository.findByCart_Id(cartId)
                .stream()
                .map(cartItemMapper::toCartItemResponse)
                .toList();
    }



    @Transactional
    public CartItem addCourseToCart(Cart cart, Course course){



        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .course(course)
                .addedAt(LocalDateTime.now())
                .build();

        return cartItemRepository.save(cartItem);
    }

    @Transactional
    public void removeCourseFromCart(String cartId,String courseId){
        boolean exists = cartItemRepository.existsByCart_IdAndCourse_Id(cartId, courseId);
        if (!exists) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        cartItemRepository.deleteByCart_IdAndCourse_Id(cartId,courseId);
    }

    @Transactional
    public void clearCart(String cartId){
        List<CartItem> items = cartItemRepository.findByCart_Id(cartId);
        cartItemRepository.deleteAll(items);
    }


    public boolean isCourseInCart(String cartId,String courseId){


        return cartItemRepository.existsByCart_IdAndCourse_Id(cartId,courseId);
    }


    public long countItems(String cartId){
        return cartItemRepository.countByCart_Id(cartId);
    }








}
