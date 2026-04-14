package com.hyeyuns2.kidtale.external.ai.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "gemini.api")
public record GeminiProperties(
        @NotBlank String baseUrl,
        @NotBlank String key,
        @NotBlank String model,
        String fallbackModel
) {
}
