package com.example.E_Learning_Platform.controller;

import com.example.E_Learning_Platform.dto.request.ChatAskRequest;
import com.example.E_Learning_Platform.dto.response.ApiResponse;
import com.example.E_Learning_Platform.dto.response.ChatAnswerResponse;
import com.example.E_Learning_Platform.entity.pg.ChatHistory;
import com.example.E_Learning_Platform.service.ChatHistoryService;
import com.example.E_Learning_Platform.service.ChatService;
import com.example.E_Learning_Platform.service.RagIndexerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final RagIndexerService ragIndexerService;
    private final ChatHistoryService chatHistoryService;

    @PostMapping("/ask")
    ApiResponse<ChatAnswerResponse> ask(@RequestBody @Valid ChatAskRequest req) {
        return ApiResponse.<ChatAnswerResponse>builder()
                .message("Ask successful")
                .result(chatService.ask(req))
                .build();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/reindex")
    ApiResponse<String> reindex() {
        ragIndexerService.reindexAll();
        return ApiResponse.<String>builder().message("Reindex successful").result("OK").build();
    }

    @GetMapping("/history")
    ApiResponse<Page<ChatHistory>> history(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.<Page<ChatHistory>>builder()
                .message("Get history successful")
                .result(chatHistoryService.getMyHistory(page, size))
                .build();
    }
}

