package com.rag.backend.chatbot.history.mapper;

import com.rag.backend.chatbot.history.dto.ChatMessageResponse;
import com.rag.backend.chatbot.history.entity.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ChatMessageMapper {

    ChatMessageResponse toResponse(ChatMessage chatMessage);

    List<ChatMessageResponse> toResponseList(List<ChatMessage> chatMessages);
}