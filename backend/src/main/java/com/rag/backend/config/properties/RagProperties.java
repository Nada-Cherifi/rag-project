package com.rag.backend.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "rag")
public class RagProperties {

    private Search search = new Search();
    private Splitter splitter = new Splitter();
    private Memory memory = new Memory();

    @Getter
    @Setter
    public static class Search {
        private int topK;
        private double similarityThreshold;
    }

    @Getter
    @Setter
    public static class Splitter {
        private int chunkSize;
        private int minChunkSizeChars;
        private int minChunkLengthToEmbed;
        private int maxNumChunks;
        private boolean keepSeparator;
    }

    @Getter
    @Setter
    public static class Memory {
        private int maxMessages;
    }

}