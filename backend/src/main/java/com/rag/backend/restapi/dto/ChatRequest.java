package com.rag.backend.restapi.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        
        @NotBlank
        String message
) {
}