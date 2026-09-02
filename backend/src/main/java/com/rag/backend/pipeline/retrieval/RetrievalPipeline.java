package com.rag.backend.pipeline.retrieval;

import com.rag.backend.config.properties.RagProperties;
import com.rag.backend.pipeline.retrieval.transformer.NomicEmbedQueryPrefixTransformer;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/*
--------------------------------------------------------------------------------
RETRIEVAL PIPELINE : question → embedding → documents pertinents → contexte RAG
--------------------------------------------------------------------------------
    User question
        ↓
    ChatMemory
        ↓
    RetrievalAugmentationAdvisor
        ↓
    NomicQueryPrefixTransformer
        ↓
    search_query: User Query
        ↓
    VectorStoreDocumentRetriever
        ↓
    EmbeddingModel
        ↓
    PgVector
        ↓
    Relevant Documents
        ↓
    ContextualQueryAugmenter
        ↓
    rag-context-prompt.st
        ↓
    Query Augmentée
*/
@Configuration
public class RetrievalPipeline {

    @Value("classpath:/prompts/rag-context-prompt.st")
    private Resource ragPromptResource;

    @Bean
    VectorStoreDocumentRetriever documentRetriever(VectorStore vectorStore,
            RagProperties ragProperties) {
        return VectorStoreDocumentRetriever.builder()
            .vectorStore(vectorStore)
            .topK(ragProperties.getSearch().getTopK())
            .similarityThreshold(ragProperties.getSearch().getSimilarityThreshold())
            .build();
    }

    @Bean
    QueryAugmenter queryAugmenter() {
        PromptTemplate ragPromptTemplate =
                PromptTemplate.builder()
                        .resource(ragPromptResource)
                        .build();

        return ContextualQueryAugmenter.builder()
                .promptTemplate(ragPromptTemplate)
                .allowEmptyContext(false)
                .build();
    }

    @Bean
    RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(
        NomicEmbedQueryPrefixTransformer queryPrefixTransformer,
        VectorStoreDocumentRetriever documentRetriever,
        QueryAugmenter queryAugmenter
    ) {
        return RetrievalAugmentationAdvisor.builder()
                .queryTransformers(queryPrefixTransformer)
                .documentRetriever(documentRetriever)
                .queryAugmenter(queryAugmenter)
                .build();
    }
}