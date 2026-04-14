package com.hyeyuns2.kidtale.external.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiGenerationConfig(
        double temperature,
        int maxOutputTokens,
        String responseMimeType
) {
    public static GeminiGenerationConfig jsonMode() {
        return new GeminiGenerationConfig(0.7, 8192, "application/json");
    }
}
