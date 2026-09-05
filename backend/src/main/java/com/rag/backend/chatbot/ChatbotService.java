package com.rag.backend.chatbot;

import com.rag.backend.chatbot.history.ChatHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

/*
CHATBOT
mémoire + retrieval + modèle de chat LLM → réponse utilisateur -> Chat History
*/
@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final ChatClient chatClient;
    private final ChatHistoryService chatHistoryService;

    public Flux<String> askStream(UUID conversationId, String question) {

        Mono<StreamingExchange> startExchange =
                Mono.fromCallable(() -> {
                            UUID assistantMessageId =
                                    chatHistoryService.startExchange(
                                            conversationId,
                                            question
                                    );
                            return new StreamingExchange(
                                    assistantMessageId,
                                    new StringBuilder()
                            );
                        })
                        .subscribeOn(
                                Schedulers.boundedElastic()
                        );

        return Flux.usingWhen(
                // 1. Préparation
                startExchange,
                // 2. Stream principal
                exchange ->
                        chatClient.prompt()
                                .user(question)
                                .advisors(advisor ->
                                        advisor.param(
                                                ChatMemory.CONVERSATION_ID,
                                                conversationId.toString()
                                        )
                                )
                                .stream()
                                .content()
                                //Pendant la génération LLM ne fait aucun UPDATE SQL par token: On accumule seulement
                                .doOnNext(chunk ->
                                        exchange
                                                .content()
                                                .append(chunk)
                                ),
                // 3. Fin normale; Une seule mise à jour finale managé par Transactional
                exchange ->
                        Mono.fromRunnable(() ->
                                        chatHistoryService
                                                .completeAssistantMessage(
                                                        exchange.assistantMessageId(),
                                                        exchange.content().toString()
                                                )
                                )
                                .subscribeOn(
                                        Schedulers.boundedElastic()
                                ),
                // 4. Erreur
                (exchange, error) ->
                        Mono.fromRunnable(() ->
                                        chatHistoryService
                                                .failAssistantMessage(
                                                        exchange.assistantMessageId(),
                                                        exchange.content().toString(),
                                                        error
                                                )
                                )
                                .subscribeOn(
                                        Schedulers.boundedElastic()
                                ),
                // 5. Annulation du client
                exchange ->
                        Mono.fromRunnable(() ->
                                        chatHistoryService
                                                .cancelAssistantMessage(
                                                        exchange.assistantMessageId(),
                                                        exchange.content().toString()
                                                )
                                )
                                .subscribeOn(
                                        Schedulers.boundedElastic()
                                )
        );
    }
}