package com.example.E_Learning_Platform.controller;

import com.example.E_Learning_Platform.configuration.OpenApiConfig;
import com.example.E_Learning_Platform.dto.request.UserCreationRequest;
import com.example.E_Learning_Platform.dto.response.ApiResponse;
import com.example.E_Learning_Platform.dto.response.UserResponse;
import com.example.E_Learning_Platform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @Operation(
            summary = "Create user",
            description = "Create a new user",
            security = {}
    )
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        log.info("Controller:createUser");
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    @GetMapping("/{userId}")
    @Operation(
            summary = "Get user by id",
            description = "Get user details by id"
    )
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    public ApiResponse<UserResponse> getUser(@PathVariable("userId") String userId) {
        log.info("Controller:getUser");
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUser(userId))
                .build();
    }

     @GetMapping
    @Operation(
            summary = "Get all users",
            description = "Get details of all users"
    ) 
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    public ApiResponse<List<UserResponse>> getUsers() {
        log.info("Controller:get All users");
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getUsers())
                .build();
    }

@GetMapping("/my-profile")
    @Operation(
            summary = "Get my profile",
            description = "Get my profile details"
    )
    
    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
    public ApiResponse<UserResponse> getMyProfile() {
        log.info("Controller:getMyProfile");
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

}
       

