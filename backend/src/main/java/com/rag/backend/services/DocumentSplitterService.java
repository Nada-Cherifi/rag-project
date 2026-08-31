package com.rag.backend.services;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

import com.rag.backend.config.RagProperties;

import java.util.List;

@Service
public class DocumentSplitterService {

    private final TokenTextSplitter splitter;

    public DocumentSplitterService(RagProperties ragProperties) {
        RagProperties.Splitter config = ragProperties.getSplitter();

        this.splitter = TokenTextSplitter.builder()
                .withChunkSize(config.getChunkSize())
                .withMinChunkSizeChars(config.getMinChunkSizeChars())
                .withMinChunkLengthToEmbed(config.getMinChunkLengthToEmbed())
                .withMaxNumChunks(config.getMaxNumChunks())
                .withKeepSeparator(config.isKeepSeparator())
                .build();
    }

    public List<Document> split(Document document) {
        return splitter.apply(List.of(document));
    }
}