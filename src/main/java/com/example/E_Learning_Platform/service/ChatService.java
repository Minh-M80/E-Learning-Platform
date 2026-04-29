// sample: service/ChatService.java
package com.example.E_Learning_Platform.service;

import com.example.E_Learning_Platform.dto.request.ChatAskRequest;
import com.example.E_Learning_Platform.dto.response.ChatAnswerResponse;
import com.example.E_Learning_Platform.entity.pg.ChatHistory;
import com.example.E_Learning_Platform.repository.pg.ChatHistoryRepository;
import com.example.E_Learning_Platform.service.rag.RagAccessService;
import com.example.E_Learning_Platform.service.rag.RagScope;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final VectorStore vectorStore;
    private final ChatClient.Builder chatClientBuilder;
    private final ChatHistoryRepository chatHistoryRepository;
    private final RagAccessService ragAccessService;

    public ChatAnswerResponse ask(ChatAskRequest req) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        RagScope scope = ragAccessService.buildScopeByEmail(email);

        List<Document> hits = vectorStore.similaritySearch(
                SearchRequest.builder().query(req.message()).topK(30).build());

        List<Document> allowed = hits.stream()
                .filter(d -> ragAccessService.canRead(d, scope))
                .limit(8)
                .toList();

        String context = allowed.stream()
                .map(d -> {
                    String type = String.valueOf(d.getMetadata().getOrDefault("entityType", "unknown"));
                    String entityId = String.valueOf(d.getMetadata().getOrDefault("entityId", "n/a"));
                    String chunkIndex = String.valueOf(d.getMetadata().getOrDefault("chunkIndex", "-"));
                    return "[type=%s, entityId=%s, chunk=%s]\n%s".formatted(type, entityId, chunkIndex, d.getText());
                })
                .collect(Collectors.joining("\n\n---\n\n"));

        String prompt = """
                Ban la tro ly E-learning.
                Chi dung du lieu trong CONTEXT, khong bia them.
                Neu thieu du lieu, tra loi dung: "Khong du thong tin".
                Tra loi theo format:
                1) Tra loi ngan gon
                2) Chi tiet chinh
                3) Nguon tham chieu (entityType/entityId neu co)
                CONTEXT:
                %s

                QUESTION:
                %s
                """.formatted(context, req.message());

        String answer = chatClientBuilder.build().prompt().user(prompt).call().content();

        ChatHistory h = chatHistoryRepository.save(ChatHistory.builder()
                .userId(scope.userId()) // khong tin userId tu request
                .userMessage(req.message())
                .aiResponse(answer)
                .createdAt(LocalDateTime.now())
                .build());

        return new ChatAnswerResponse(answer, h.getCreatedAt());
    }
}
