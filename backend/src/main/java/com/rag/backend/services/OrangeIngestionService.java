package com.rag.backend.services;


import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class OrangeIngestionService {

    private final OrangeWebFetcher webFetcher;
    private final OrangeTutorialExtractor extractor;
    private final DocumentSplitterService splitterService;
    private final VectorStore vectorStore;

    public OrangeIngestionService(
            OrangeWebFetcher webFetcher,
            OrangeTutorialExtractor extractor,
            DocumentSplitterService splitterService,
            VectorStore vectorStore
    ) {
        this.webFetcher = webFetcher;
        this.extractor = extractor;
        this.splitterService = splitterService;
        this.vectorStore = vectorStore;
    }

    public int ingest(String url) throws IOException {

        // 1. Télécharger la page Orange
        org.jsoup.nodes.Document html =
                webFetcher.fetch(url);

        // 2. Transformer HTML -> Spring AI Document
        Document document =
                extractor.extract(html, url);

        // 3. Découper
        List<Document> chunks =
                splitterService.split(document);

        // 4. Embeddings + PostgreSQL/pgvector
        vectorStore.add(chunks);

        return chunks.size();
    }
}