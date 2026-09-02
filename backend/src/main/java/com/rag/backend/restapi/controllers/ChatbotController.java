package com.rag.backend.restapi.controllers;

import com.rag.backend.chatbot.ChatbotService;
import com.rag.backend.restapi.dto.ChatRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping(
            value = "/stream",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
 //           produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<String> askStream(@Valid @RequestBody ChatRequest request) {
        return chatbotService.askStream(request.conversationId(), request.message());
    }
}