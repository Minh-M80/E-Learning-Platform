// repository/pg/ChatHistoryRepository.java
package com.example.E_Learning_Platform.repository.pg;

import com.example.E_Learning_Platform.entity.pg.ChatHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, String> {
    Page<ChatHistory> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
}
