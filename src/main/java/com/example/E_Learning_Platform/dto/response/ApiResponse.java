/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.E_Learning_Platform.dto.response;

import lombok.Builder;

/**
 *
 * @author admin
 */
public class ApiResponse<T> {
    @Builder.Default
    int code = 1000;

    String message;
    T result;
}
