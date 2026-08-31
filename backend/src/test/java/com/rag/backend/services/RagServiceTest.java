package com.rag.backend.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RagServiceTest {

    @Autowired
    private RagService ragService;

    @Test
    void shouldAnswerUsingOrangeDocumentation() {

        String question =
                "J'ai des problèmes de connexion sur ma Livebox?";

        String response =
                ragService.ask(question);

        System.out.println();
        System.out.println("Question :");
        System.out.println(question);

        System.out.println();
        System.out.println("Réponse :");
        System.out.println(response);

        assertThat(response).isNotBlank();
    }
}