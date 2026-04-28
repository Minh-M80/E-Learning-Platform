/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.E_Learning_Platform.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 *
 * @author admin
 */
public record ChatAskRequest(
         @NotBlank String userId,
        @NotBlank String message
) {}
