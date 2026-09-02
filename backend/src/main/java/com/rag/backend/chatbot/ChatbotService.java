package com.rag.backend.chatbot;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
/*
CHATBOT
mémoire + retrieval + modèle de chat LLM → réponse utilisateur
*/
@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final ChatClient chatClient;

    public Flux<String> askStream(String conversationId, String message) {
        return chatClient.prompt()
                .user(message)
                .advisors(advisorSpec ->
                    advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId)
                )
                .stream()
                .content();
    }

}