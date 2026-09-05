package com.rag.backend.chatbot.history.mapper;

import com.rag.backend.chatbot.history.dto.ConversationResponse;
import com.rag.backend.chatbot.history.entity.Conversation;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ConversationMapper {

    ConversationResponse toResponse(Conversation conversation);

    List<ConversationResponse> toResponseList(List<Conversation> conversations);
}