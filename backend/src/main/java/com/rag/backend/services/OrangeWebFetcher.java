package com.rag.backend.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import com.rag.backend.config.properties.OrangeAssistanceProperties;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

@Service
public class OrangeWebFetcher {

    private final OrangeAssistanceProperties orangeProperties;

    public OrangeWebFetcher(OrangeAssistanceProperties properties) {
        this.orangeProperties = properties;
    }

    public Document fetch(String url) throws IOException {
        final var host = URI.create(url).getHost();

        if (host == null ||
                orangeProperties.getAllowedHosts().stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(allowedHost -> !allowedHost.isBlank())
                        .noneMatch(allowedHost ->
                                allowedHost.equalsIgnoreCase(host))) {
            throw new IllegalArgumentException(
                    "Domaine non autorisé : " + host
            );
        }

        return Jsoup.connect(url)
                .userAgent(orangeProperties.getUserAgent())
                .timeout(orangeProperties.getTimeout())
                .get();
    }

}