package com.rag.backend.pipeline.ingestion.reader;

import com.rag.backend.config.properties.OrangeAssistanceProperties;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/*
    URL Orange
        ↓
    récupération HTML
        ↓
    extraction texte
        ↓
    création Document Spring AI
*/
@Component
@RequiredArgsConstructor
public class OrangeAssistanceDocumentReader {

    private final OrangeAssistanceProperties properties;

    public List<Document> read(String url) throws IOException {
        validateUrl(url);

        var html = Jsoup.connect(url)
            .userAgent(properties.getUserAgent())
            .timeout(properties.getTimeout())
            .get();
        var title = html.title();
        var main = html.selectFirst("main");
        var content = main != null
                ? main.text()
                : html.body().text();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sourceUrl", url);
        metadata.put("title", title);
        metadata.put("source", "Orange Assistance");
        metadata.put("type", "tutorial");

        Document document = new Document(content, metadata);

        return List.of(document);
    }

    private void validateUrl(String url) {
        final var host = URI.create(url).getHost();

        if (host == null) 
            throw new IllegalArgumentException("URL invalide : " + url);

        if (properties.getAllowedHosts()
                .stream()
                .filter(Objects::nonNull)
                .map(value -> value.trim())
                .filter(value -> !value.isBlank())
                .noneMatch(value -> value.equalsIgnoreCase(host)))
            throw new IllegalArgumentException("Domaine non autorisé : " + host);
    }
}