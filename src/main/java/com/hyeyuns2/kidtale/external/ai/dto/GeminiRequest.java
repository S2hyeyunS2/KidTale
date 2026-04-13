package com.hyeyuns2.kidtale.external.ai.dto;

import java.util.List;

public record GeminiRequest(
        List<GeminiContent> contents,
        GeminiGenerationConfig generationConfig
) {}
