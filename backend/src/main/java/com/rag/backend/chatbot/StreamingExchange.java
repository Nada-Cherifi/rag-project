package com.rag.backend.chatbot;

import java.util.UUID;

public record StreamingExchange(
        UUID assistantMessageId,
        StringBuilder content
) {
}