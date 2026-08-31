package com.rag.backend.controllers;
import org.springframework.web.bind.annotation.*;

import com.rag.backend.services.OrangeIngestionService;

import java.io.IOException;
import java.util.Map;

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
}