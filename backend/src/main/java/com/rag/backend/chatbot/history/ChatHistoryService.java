package com.rag.backend.chatbot.history;

import com.rag.backend.chatbot.history.dto.ChatMessageResponse;
import com.rag.backend.chatbot.history.dto.ConversationResponse;
import com.rag.backend.chatbot.history.entity.ChatMessage;
import com.rag.backend.chatbot.history.entity.Conversation;
import com.rag.backend.chatbot.history.mapper.ChatMessageMapper;
import com.rag.backend.chatbot.history.mapper.ConversationMapper;
import com.rag.backend.chatbot.history.model.MessageRole;
import com.rag.backend.chatbot.history.model.MessageStatus;
import com.rag.backend.chatbot.history.repository.ChatMessageRepository;
import com.rag.backend.chatbot.history.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private static final String NEW_CONVERSATION_TITLE = "Nouvelle conversation";

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ConversationMapper conversationMapper;
    private final ChatMessageMapper chatMessageMapper;

    @Transactional
    public ConversationResponse createConversation() {
        Conversation conversation = new Conversation();
        conversation.setTitle(NEW_CONVERSATION_TITLE);

        Conversation savedConversation = conversationRepository.save(conversation);

        return conversationMapper.toResponse(savedConversation);
    }

    @Transactional
    public UUID startExchange(UUID conversationId, String userContent) {
        requireConversation(conversationId);

        ChatMessage userMessage = new ChatMessage();
        userMessage.setConversationId(conversationId);
        userMessage.setRole(MessageRole.USER);
        userMessage.setStatus(MessageStatus.COMPLETED);
        userMessage.setContent(userContent);

        chatMessageRepository.save(userMessage);

        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setConversationId(conversationId);
        assistantMessage.setRole(MessageRole.ASSISTANT);
        assistantMessage.setStatus(MessageStatus.STREAMING);
        assistantMessage.setContent("");

        ChatMessage saved = chatMessageRepository.save(assistantMessage);

        return saved.getId();
    }


    @Transactional
    public void completeAssistantMessage(UUID messageId, String content) {
        ChatMessage message = requireMessage(messageId);

        message.setContent(content);
        message.setStatus(MessageStatus.COMPLETED);
        message.setErrorMessage(null);
    }


    @Transactional
    public void failAssistantMessage(UUID messageId, String partialContent,
            Throwable error
    ) {

        ChatMessage message = requireMessage(messageId);

        message.setContent(partialContent);
        message.setStatus(MessageStatus.FAILED);
        message.setErrorMessage(error.getMessage());
    }


    @Transactional
    public void cancelAssistantMessage(UUID messageId, String partialContent) {
        ChatMessage message = requireMessage(messageId);

        message.setContent(partialContent);
        message.setStatus(MessageStatus.CANCELLED);
    }

 
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getHistory(UUID conversationId) {
        return chatMessageMapper.toResponseList(
                chatMessageRepository
                        .findByConversationIdOrderByCreatedAtAsc(
                                conversationId
                        )
        );
    }

    @Transactional
    public void deleteConversation(UUID conversationId) {
        requireConversation(conversationId);
        chatMessageRepository.deleteByConversationId(conversationId);
        conversationRepository.deleteById(conversationId);
    }

    private Conversation requireConversation(UUID conversationId) {
        return conversationRepository
                .findById(conversationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Conversation introuvable : "
                                        + conversationId
                        )
                );
    }


    private ChatMessage requireMessage(UUID messageId) {
        return chatMessageRepository
                .findById(messageId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Message introuvable : "
                                        + messageId
                        )
                );
    }
}
