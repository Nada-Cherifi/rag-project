package com.rag.backend.chatbot;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
/*
construit/configure le ChatClient utilisé par chatbot.
intercepts, inspects, and optimizes mid-generation steps before LLM
 ChatClient Factory = 
    ChatModel
        +
    System Prompt
        +
    ChatMemory Advisor
        +
    Retrieval Advisor
*/
@Configuration
public class ChatbotClientFactory {

    @Value("classpath:/prompts/chatbot-system-prompt.st")
    private Resource systemPromptResource;


    @Bean
    ChatClient chatClient(
            ChatModel chatModel,
            MessageChatMemoryAdvisor memoryAdvisor,
            RetrievalAugmentationAdvisor retrievalAdvisor
    ) {
        return ChatClient.builder(chatModel)
                .defaultSystem(systemPromptResource)
                .defaultAdvisors(memoryAdvisor, retrievalAdvisor)
                .build();
    }
}