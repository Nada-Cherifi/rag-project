package com.rag.backend.restapi.controllers;

import com.rag.backend.services.OrangeIngestionService;
import com.rag.backend.restapi.dto.IndexedSourceResponse;

import java.io.IOException;
import java.util.Map;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rag/ingestion")
public class OrangeIngestionController {

    private final OrangeIngestionService ingestionService;

    public OrangeIngestionController(
            OrangeIngestionService ingestionService
    ) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/orange")
    public Map<String, Object> ingest(
            @RequestParam String url
    ) throws IOException {

        int chunks = ingestionService.ingest(url);

        return Map.of(
                "url", url,
                "chunks", chunks,
                "status", "INGESTED"
        );
    }

    @GetMapping("/sources")
    public List<IndexedSourceResponse> listSources() {
        return ingestionService.listSources();
    }

    @DeleteMapping("/orange")
    public Map<String, Object> delete(@RequestParam String url) {
        int deletedChunks = ingestionService.deleteSource(url);
        return Map.of(
                "url", url,
                "deletedChunks", deletedChunks,
                "status", "DELETED"
        );
    }
}
