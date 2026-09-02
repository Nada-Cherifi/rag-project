package com.rag.backend.pipeline.ingestion;

import com.rag.backend.pipeline.ingestion.reader.OrangeAssistanceDocumentReader;
import com.rag.backend.pipeline.ingestion.transformer.DocumentSplitter;
import com.rag.backend.pipeline.ingestion.transformer.NomicEmbedDocumentPrefixTransformer;
import com.rag.backend.pipeline.ingestion.writer.VectorStoreDocumentWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

/*
--------------------------------------------------------
INGESTION PIPELINE : documents → embeddings → PgVector
--------------------------------------------------------
    Orange URL
        ↓
    OrangeAssistanceDocumentReader
        ↓
    List<Document>
        ↓
    DocumentSplitter
        ↓
    List<Document chunks>
        ↓
    NomicDocumentPrefixTransformer
        ↓
    search_document: ...
        ↓
    VectorStoreDocumentWriter
        ↓
    EmbeddingModel
        ↓
    PgVector
*/
@Service
@RequiredArgsConstructor
public class OrangeIngestionPipeline {

    private final OrangeAssistanceDocumentReader reader;
    private final DocumentSplitter splitter;
    private final NomicEmbedDocumentPrefixTransformer documentPrefixTransformer;
    private final VectorStoreDocumentWriter writer;

    public int ingest(String url) throws IOException {
        var documents = reader.read(url);

        var chunks = splitter.apply(documents);

        var embeddingDocuments = documentPrefixTransformer.apply(chunks);

        writer.accept(embeddingDocuments);

        return embeddingDocuments.size();
    }
}