package com.hyeyuns2.kidtale.external.sweetbook.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "sweetbook.api")
public record SweetBookProperties(
        @NotBlank String baseUrl,
        @NotBlank String key,
        String bookSpecUid  // 기본 판형 UID (e.g. SQUAREBOOK_HC). 미설정 시 SQUAREBOOK_HC 사용
) {
}
