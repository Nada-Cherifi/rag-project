package com.rag.backend.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class RagConfig {

    @Value("classpath:/prompts/system-prompt.st")
    private Resource systemPromptResource;

    @Value("classpath:/prompts/rag-advisor-prompt.st")
    private Resource ragPromptResource;
 
    private final RagProperties ragProperties;

    public RagConfig(RagProperties properties) {
        this.ragProperties = properties;
    }

    @Bean
    QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore) {
        SearchRequest searchRequest =
                SearchRequest.builder()
                    .topK(ragProperties.getSearch().getTopK())
                    .similarityThreshold(ragProperties.getSearch().getSimilarityThreshold())
                    .build();

        PromptTemplate ragPromptTemplate = new PromptTemplate(ragPromptResource);

        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(searchRequest)
                .promptTemplate(ragPromptTemplate)
                .build();
    }


    @Bean
    ChatClient chatClient(ChatModel chatModel, QuestionAnswerAdvisor questionAnswerAdvisor) {
        return ChatClient.builder(chatModel)
                .defaultSystem(systemPromptResource)
                .defaultAdvisors(questionAnswerAdvisor)
                .build();
    }
}