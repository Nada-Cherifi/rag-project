package com.rag.backend.chatbot.history.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rag.backend.chatbot.history.entity.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
}
