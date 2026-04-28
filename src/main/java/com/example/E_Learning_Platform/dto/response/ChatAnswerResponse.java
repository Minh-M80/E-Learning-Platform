// dto/response/ChatAnswerResponse.java
package com.example.E_Learning_Platform.dto.response;

import java.time.LocalDateTime;

public record ChatAnswerResponse(
        String answer,
        LocalDateTime createdAt
) {}
