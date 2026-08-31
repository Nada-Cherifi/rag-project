package com.rag.backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class RagService {

    private final ChatClient chatClient;

    public String ask(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    public Flux<String> askStream(String question) {
        return chatClient.prompt()
            .user(question)
            .stream()
            .content();
    }
}