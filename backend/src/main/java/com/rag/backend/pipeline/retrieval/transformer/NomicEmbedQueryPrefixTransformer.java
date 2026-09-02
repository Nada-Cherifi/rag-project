package com.rag.backend.pipeline.retrieval.transformer;

import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.stereotype.Component;

/*
    User Query
      ↓
    search_query: User Query
*/
@Component
public class NomicEmbedQueryPrefixTransformer implements QueryTransformer {

    private static final String QUERY_PREFIX = "search_query: ";

    @Override
    public Query transform(Query query) {
        final var text = query.text();

        if (text == null || text.isBlank())
            return query;

        if (text.startsWith(QUERY_PREFIX))
            return query;

        return Query.builder()
                .text(QUERY_PREFIX + text)
                .history(query.history())
                .context(query.context())
                .build();
    }
}