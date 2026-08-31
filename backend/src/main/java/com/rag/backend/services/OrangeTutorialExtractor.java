package com.rag.backend.services;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class OrangeTutorialExtractor {

    public Document extract(
            org.jsoup.nodes.Document html,
            String url
    ) {

        final var title = html.title();

        final var main = html.selectFirst("main");

        String content;

        if (main != null) {
            content = main.text();
        } else {
            content = html.body().text();
        }

        Map<String, Object> metadata = new HashMap<>();

        metadata.put("sourceUrl", url);
        metadata.put("title", title);
        metadata.put("source", "Orange Assistance");
        metadata.put("type", "tutorial");

        return new Document(content, metadata);
    }
}