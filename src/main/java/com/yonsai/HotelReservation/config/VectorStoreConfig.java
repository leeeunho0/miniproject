package com.yonsai.HotelReservation.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorStoreConfig {

  // spring-ai-starter-model-openai가 자동 등록해주는 EmbeddingModel(OpenAI)을 받아서
  // 텍스트를 벡터로 바꾼 뒤 메모리에 저장하는 SimpleVectorStore 빈을 직접 만들어줌
  @Bean
  public VectorStore vectorStore(EmbeddingModel embeddingModel) {
    return SimpleVectorStore.builder(embeddingModel).build();
  }
}