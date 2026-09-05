package com.rag.backend.chatbot.history.dto;

import java.time.Instant;
import java.util.UUID;

import com.rag.backend.chatbot.history.model.MessageRole;
import com.rag.backend.chatbot.history.model.MessageStatus;

public record ChatMessageResponse(
        UUID id,
        MessageRole role,
        MessageStatus status,
        String content,
        String errorMessage,
        Instant createdAt

) {
}