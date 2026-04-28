
package com.example.E_Learning_Platform.service;

import com.example.E_Learning_Platform.entity.pg.ChatHistory;
import com.example.E_Learning_Platform.exception.AppException;
import com.example.E_Learning_Platform.exception.ErrorCode;
import com.example.E_Learning_Platform.repository.UserRepository;
import com.example.E_Learning_Platform.repository.pg.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private final ChatHistoryRepository chatHistoryRepository;
    private final UserRepository userRepository;

    public Page<ChatHistory> getMyHistory(int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        String userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED))
                .getId();

        return chatHistoryRepository.findByUserIdOrderByCreatedAtDesc(
                userId,
                PageRequest.of(page, size)
        );
    }
}
