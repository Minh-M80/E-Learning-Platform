package com.example.E_Learning_Platform.configuration;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddingConfig {

    @Bean(name = "customOpenAiEmbeddingModel")
    EmbeddingModel embeddingModel(
            @Value("${app.openai-embedding.api-key}") String apiKey,
            @Value("${app.openai-embedding.model}") String model) {

        OpenAiApi api = OpenAiApi.builder()
                .apiKey(apiKey)
                .baseUrl("https://api.openai.com")
                .build();

        return new OpenAiEmbeddingModel(
                api,
                org.springframework.ai.document.MetadataMode.EMBED,
                OpenAiEmbeddingOptions.builder().model(model).build()
        );
    }
}
