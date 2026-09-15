package com.rag.backend.services;


import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.rag.backend.restapi.dto.IndexedSourceResponse;

import java.io.IOException;
import java.util.List;

@Service
public class OrangeIngestionService {

    private final OrangeWebFetcher webFetcher;
    private final OrangeTutorialExtractor extractor;
    private final DocumentSplitterService splitterService;
    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;

    public OrangeIngestionService(
            OrangeWebFetcher webFetcher,
            OrangeTutorialExtractor extractor,
            DocumentSplitterService splitterService,
            VectorStore vectorStore,
            JdbcTemplate jdbcTemplate
    ) {
        this.webFetcher = webFetcher;
        this.extractor = extractor;
        this.splitterService = splitterService;
        this.vectorStore = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
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

    public List<IndexedSourceResponse> listSources() {
        return jdbcTemplate.query("""
                SELECT metadata ->> 'sourceUrl' AS url,
                       COUNT(*) AS chunks,
                       MAX(created_at) AS indexed_at
                FROM vector_store
                WHERE metadata ? 'sourceUrl'
                GROUP BY metadata ->> 'sourceUrl'
                ORDER BY MAX(created_at) DESC
                """, (resultSet, rowNumber) -> new IndexedSourceResponse(
                resultSet.getString("url"),
                resultSet.getLong("chunks"),
                resultSet.getTimestamp("indexed_at").toInstant()
        ));
    }

    public int deleteSource(String url) {
        return jdbcTemplate.update(
                "DELETE FROM vector_store WHERE metadata ->> 'sourceUrl' = ?",
                url
        );
    }
}
