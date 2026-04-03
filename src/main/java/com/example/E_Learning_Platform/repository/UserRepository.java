/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.example.E_Learning_Platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.E_Learning_Platform.entity.User;

import java.util.Optional;

/**
 *
 * @author admin
 */
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
