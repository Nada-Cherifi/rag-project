package com.rag.backend.restapi.controllers;

import com.rag.backend.chatbot.ChatbotService;
import com.rag.backend.chatbot.history.ChatHistoryService;
import com.rag.backend.chatbot.history.dto.ChatMessageResponse;
import com.rag.backend.chatbot.history.dto.ConversationResponse;
import com.rag.backend.restapi.dto.ChatRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatHistoryService chatHistoryService;
    private final ChatbotService chatbotService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationResponse createConversation() {
        return chatHistoryService.createConversation();
    }

    @GetMapping("/{conversationId}/messages")
    public List<ChatMessageResponse> getHistory(@PathVariable UUID conversationId) {
        return chatHistoryService.getHistory(conversationId);
    }

    @DeleteMapping("/{conversationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConversation(@PathVariable UUID conversationId) {
        chatHistoryService.deleteConversation(conversationId);
    }

    @PostMapping(
            value = "/{conversationId}/messages/stream",
            consumes = MediaType.APPLICATION_JSON_VALUE,
          produces = MediaType.TEXT_EVENT_STREAM_VALUE
 //           produces = MediaType.TEXT_PLAIN_VALUE
    )
    public Flux<String> askStream(
            @PathVariable UUID conversationId,
            @Valid @RequestBody ChatRequest request) {
        return chatbotService.askStream(conversationId, request.message());
    }

}
