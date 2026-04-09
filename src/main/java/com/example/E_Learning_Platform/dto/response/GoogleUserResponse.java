package com.example.E_Learning_Platform.dto.response;

import lombok.Data;

@Data
public class GoogleUserResponse {
    private String sub; // ID duy nhất của người dùng từ Google
    private String name; // Tên đầy đủ
    private String given_name; // Tên
    private String family_name; // Họ
    private String picture; // Avatar
    private String email; // Email
    private boolean email_verified;
    private String locale;
}
