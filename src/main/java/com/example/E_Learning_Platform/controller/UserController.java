package com.example.E_Learning_Platform.controller;

import com.example.E_Learning_Platform.configuration.OpenApiConfig;
import com.example.E_Learning_Platform.dto.request.RoleUpdateRequest;
import com.example.E_Learning_Platform.dto.request.UserCreationRequest;
import com.example.E_Learning_Platform.dto.request.UserRequest;
import com.example.E_Learning_Platform.dto.request.UserUpdateRequest;
import com.example.E_Learning_Platform.dto.response.ApiResponse;
import com.example.E_Learning_Platform.dto.response.UserResponse;
import com.example.E_Learning_Platform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
     ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
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
//    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
     ApiResponse<UserResponse> getUser(@PathVariable("userId") String userId) {
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
//    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
     ApiResponse<List<UserResponse>> getUsers() {
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
    
//    @SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
     ApiResponse<UserResponse> getMyProfile() {
        log.info("Controller:getMyProfile");
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @PutMapping ()
    @Operation(
            summary = "Update User",
            description = "User user"
    )


     ApiResponse<UserResponse> updateUser(UserUpdateRequest request) {
        log.info("Controller:getMyProfile");
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUser(request))
                .build();
    }

    @PutMapping("/{userId}/roles")

    @Operation(summary = "Update user roles (Admin only)")
    ApiResponse<UserResponse> updateUserRoles(
            @PathVariable String userId,
            @RequestBody @Valid RoleUpdateRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.updateUserRole(userId, request))
                .build();
    }



}
       

