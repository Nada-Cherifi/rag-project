package com.rag.backend.pipeline.ingestion.transformer;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.stereotype.Component;

import java.util.List;

/*
    chunk
      ↓
    search_document: chunk
*/
@Component
public class NomicEmbedDocumentPrefixTransformer implements DocumentTransformer {

    private static final String DOCUMENT_PREFIX = "search_document: ";

    @Override
    public List<Document> apply(List<Document> documents) {
        return documents.stream()
                .map(this::prefixDocument)
                .toList();
    }

    private Document prefixDocument(Document document) {
        var text = document.getText();

        if (text == null || text.isBlank())
            return document;

        if (text.startsWith(DOCUMENT_PREFIX))
            return document;

        return new Document(
            document.getId(),
            DOCUMENT_PREFIX + text,
            document.getMetadata()
        );
    }
}