package com.rag.backend.controllers;

import com.rag.backend.dto.ChatRequest;
import com.rag.backend.services.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatBotController {

    private final RagService ragService;

    @PostMapping
    public String ask(@RequestBody ChatRequest request) {
        return ragService.ask(request.message());
    }

    @PostMapping(
            value = "/stream",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<String> askStream(@RequestBody ChatRequest request) {
        return ragService.askStream(request.message());
    }
}