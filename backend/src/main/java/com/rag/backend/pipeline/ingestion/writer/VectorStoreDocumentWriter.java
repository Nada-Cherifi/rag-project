package com.rag.backend.pipeline.ingestion.writer;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentWriter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

/*
    List<Document>
        ↓
    EmbeddingModel
        ↓
    PgVector
*/
@Component
@RequiredArgsConstructor
public class VectorStoreDocumentWriter implements DocumentWriter {

    private final VectorStore vectorStore;

    @Override
    public void accept(List<Document> documents) {
        vectorStore.accept(documents);
    }
}