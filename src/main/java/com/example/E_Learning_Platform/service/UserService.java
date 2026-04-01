/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.E_Learning_Platform.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.E_Learning_Platform.repository.UserReporitory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author admin
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
        private final UserReporitory userReporitory;
        private final PasswordEncoder passwordEncoder;

    
}
