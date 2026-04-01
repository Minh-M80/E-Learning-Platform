/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.example.E_Learning_Platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.E_Learning_Platform.entity.User;

/**
 *
 * @author admin
 */
public interface UserReporitory extends JpaRepository<String, User> {

}
