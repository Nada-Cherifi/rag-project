package com.rag.backend.pipeline.ingestion.transformer;

import com.rag.backend.config.properties.RagProperties;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.List;

/*
    Document long
        ↓
    chunk
*/
@Component
public class DocumentSplitter implements DocumentTransformer {

    private final TokenTextSplitter tokenTextSplitter;

    public DocumentSplitter(RagProperties ragProperties) {
        RagProperties.Splitter properties = ragProperties.getSplitter();
        this.tokenTextSplitter = TokenTextSplitter.builder()
                .withChunkSize(properties.getChunkSize())
                .withMinChunkSizeChars(properties.getMinChunkSizeChars())
                .withMinChunkLengthToEmbed(properties.getMinChunkLengthToEmbed())
                .withMaxNumChunks(properties.getMaxNumChunks())
                .withKeepSeparator(properties.isKeepSeparator())
                .build();
    }

    @Override
    public List<Document> apply(List<Document> documents) {
        return tokenTextSplitter.apply(documents);
    }
}