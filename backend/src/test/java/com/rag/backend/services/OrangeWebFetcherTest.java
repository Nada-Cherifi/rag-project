package com.rag.backend.services;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrangeWebFetcherTest {

    @Autowired
    private OrangeWebFetcher orangeWebFetcher;

    @Test
    void shouldFetchOrangeAssistancePage() throws Exception {

        String url =
                "https://assistance.orange.fr/equipement/livebox-et-modems/livebox-7-sagemcom";

        Document document = orangeWebFetcher.fetch(url);

        assertThat(document).isNotNull();
        assertThat(document.title()).isNotBlank();
        assertThat(document.body()).isNotNull();

        System.out.println("TITLE : " + document.title());
        System.out.println("TEXT : " + document.body().text());
    }
}