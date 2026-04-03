/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.E_Learning_Platform.service;

import java.util.HashSet;
import java.util.List;

import com.example.E_Learning_Platform.dto.request.UserCreationRequest;
import com.example.E_Learning_Platform.dto.response.UserResponse;
import com.example.E_Learning_Platform.entity.User;
import com.example.E_Learning_Platform.enums.Role;
import com.example.E_Learning_Platform.exception.AppException;
import com.example.E_Learning_Platform.exception.ErrorCode;
import com.example.E_Learning_Platform.mapper.UserMapper;

import org.springframework.cache.annotation.Cacheable;

import com.example.E_Learning_Platform.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 *
 * @author admin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        private final UserMapper userMapper;

        public UserResponse createUser(UserCreationRequest request){
                log.info("Service:createUser");

                User user = userMapper.toUser(request);
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                if (user.getRoles() == null) {
                        user.setRoles(new HashSet<>());
                }
                user.getRoles().add(Role.STUDENT);

                try {
                        user = userRepository.save(user);
                }
                catch (DataIntegrityViolationException dataIntegrityViolationException){
                        throw  new AppException(ErrorCode.USER_EXISTED);
                }

                return userMapper.toUserResponse(user);
        }

        @PreAuthorize("hasAuthority('ADMIN')")
        public List<UserResponse> getUsers(){
                return userRepository.findAll()
                        .stream()
                        .map(userMapper::toUserResponse).toList();
        }


//        @PreAuthorize("hasAuthority('ADMIN')")
        @Cacheable(value = "users", key = "#id")
        public UserResponse getUser(String id){
                return userMapper.toUserResponse(userRepository.findById(id)
                        .orElseThrow( () -> new AppException(ErrorCode.USER_NOT_EXISTED))
                );

        }

        public UserResponse getMyInfo() {
                var context = SecurityContextHolder.getContext();
                String username = context.getAuthentication().getName();

                User user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
                return userMapper.toUserResponse(user);
        }

    
}
