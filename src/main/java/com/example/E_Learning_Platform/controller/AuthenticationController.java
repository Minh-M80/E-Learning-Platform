/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.E_Learning_Platform.controller;

import com.example.E_Learning_Platform.dto.request.AuthenticationRequest;
import com.example.E_Learning_Platform.dto.request.LogoutRequest;
import com.example.E_Learning_Platform.dto.response.ApiResponse;
import com.example.E_Learning_Platform.dto.response.AuthenticationResponse;
import com.example.E_Learning_Platform.dto.response.GoogleUserResponse;
import com.example.E_Learning_Platform.service.AuthenticationService;
import com.example.E_Learning_Platform.service.GoogleAuthService;
import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import java.text.ParseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.E_Learning_Platform.dto.request.ForgotPasswordRequest;
import com.example.E_Learning_Platform.dto.request.ResetPasswordRequest;
import com.example.E_Learning_Platform.service.PasswordResetService;


/**
 *
 * @author admin
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final PasswordResetService passwordResetService;
    private final GoogleAuthService googleAuthService;

    @PostMapping("/login")
    @Operation(security = {})
    ApiResponse<AuthenticationResponse> login(@RequestBody AuthenticationRequest request){

        var result = authenticationService.login(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/refresh")
    @Operation(security = {})
    ApiResponse<AuthenticationResponse> refreshToken(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/logout")
    @Operation(security = {})
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .message("Logout successful")
                .build();
    }

        @PostMapping("/forgot-password")
    @Operation(security = {})
    ApiResponse<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request.getEmail());
        return ApiResponse.<Void>builder()
                .message("Reset token has been sent to email")
                .build();
    }

    @PostMapping("/reset-password")
    @Operation(security = {})
    ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ApiResponse.<Void>builder()
                .message("Password reset successful")
                .build();
    }

    // API 1: Generate Login URL (để test trên trình duyệt)
    @GetMapping("/login-url")
    public ResponseEntity<String> getGoogleLoginUrl() {
        return ResponseEntity.ok(googleAuthService.generateGoogleLoginUrl());
    }

    // API 2: Nhận Code, xử lý login
    @PostMapping("/callback")
    public ApiResponse<AuthenticationResponse> authenticateWithGoogle(@RequestParam String code) {
        // Bước 1: Lấy Token của Google
        String googleAccessToken = googleAuthService.getGoogleAccessToken(code);

        // Bước 2: Dùng Token lấy thông tin (Email, Name...)
        GoogleUserResponse userInfo = googleAuthService.getGoogleUserInfo(googleAccessToken);

        // Bước 3: Xử lý đăng nhập / Đăng ký và sinh JWT thông qua Service
        AuthenticationResponse result = authenticationService.googleLogin(userInfo);

        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

}
