// configuration/PgVectorConfig.java (đổi EmbeddingModel sang @Qualifier)
package com.example.E_Learning_Platform.configuration;

import javax.sql.DataSource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class PgVectorConfig {

    @Bean
    DataSource pgVectorDataSource(
            @Value("${app.pgvector.datasource.url}") String url,
            @Value("${app.pgvector.datasource.username}") String username,
            @Value("${app.pgvector.datasource.password}") String password) {
        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url(url).username(username).password(password).build();
    }

    @Bean
    JdbcTemplate pgVectorJdbcTemplate(@Qualifier("pgVectorDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean
    VectorStore vectorStore(
            @Qualifier("pgVectorJdbcTemplate") JdbcTemplate jdbcTemplate,
            @Qualifier("customOpenAiEmbeddingModel") EmbeddingModel embeddingModel,
            @Value("${app.pgvector.initialize-schema:true}") boolean initializeSchema,
            @Value("${app.pgvector.dimensions:1536}") int dimensions) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .initializeSchema(initializeSchema)
                .dimensions(dimensions)
                .build();
    }
}
