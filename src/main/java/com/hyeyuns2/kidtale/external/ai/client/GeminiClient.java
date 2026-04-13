package com.hyeyuns2.kidtale.external.ai.client;

import com.hyeyuns2.kidtale.common.exception.ErrorCode;
import com.hyeyuns2.kidtale.common.exception.KidTaleException;
import com.hyeyuns2.kidtale.external.ai.config.GeminiProperties;
import com.hyeyuns2.kidtale.external.ai.dto.GeminiCandidate;
import com.hyeyuns2.kidtale.external.ai.dto.GeminiContent;
import com.hyeyuns2.kidtale.external.ai.dto.GeminiGenerationConfig;
import com.hyeyuns2.kidtale.external.ai.dto.GeminiPart;
import com.hyeyuns2.kidtale.external.ai.dto.GeminiRequest;
import com.hyeyuns2.kidtale.external.ai.dto.GeminiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Slf4j
@Component
public class GeminiClient {

    private static final String STORY_PROMPT_TEMPLATE = """
            아이 이름이 '%s'이고 %d살인 아이를 위한 '%s' 테마의 한국어 동화를 만들어주세요.

            요구사항:
            - %s가 주인공이어야 합니다
            - %d살에게 적합한 수준의 내용
            - 긍정적이고 교훈적인 결말
            - 반드시 10페이지로 구성하세요
            - 각 페이지는 2~3문장으로 자연스럽게 이어지는 이야기

            반드시 아래 JSON 형식으로만 출력하세요 (다른 텍스트 없이):
            {
              "title": "동화 제목",
              "pages": [
                {"pageNumber": 1, "text": "첫 번째 페이지 내용", "imageDescription": "detailed illustration description in English"},
                {"pageNumber": 2, "text": "두 번째 페이지 내용", "imageDescription": "detailed illustration description in English"},
                {"pageNumber": 3, "text": "세 번째 페이지 내용", "imageDescription": "detailed illustration description in English"},
                {"pageNumber": 4, "text": "네 번째 페이지 내용", "imageDescription": "detailed illustration description in English"},
                {"pageNumber": 5, "text": "다섯 번째 페이지 내용", "imageDescription": "detailed illustration description in English"},
                {"pageNumber": 6, "text": "여섯 번째 페이지 내용", "imageDescription": "detailed illustration description in English"},
                {"pageNumber": 7, "text": "일곱 번째 페이지 내용", "imageDescription": "detailed illustration description in English"},
                {"pageNumber": 8, "text": "여덟 번째 페이지 내용", "imageDescription": "detailed illustration description in English"},
                {"pageNumber": 9, "text": "아홉 번째 페이지 내용", "imageDescription": "detailed illustration description in English"},
                {"pageNumber": 10, "text": "열 번째 페이지 내용", "imageDescription": "detailed illustration description in English"}
              ]
            }
            """;

    private final WebClient webClient;
    private final GeminiProperties properties;

    public GeminiClient(
            @Qualifier("geminiWebClient") WebClient webClient,
            GeminiProperties properties
    ) {
        this.webClient = webClient;
        this.properties = properties;
    }

    public String generateStory(String childName, int childAge, String theme) {
        String prompt = buildPrompt(childName, childAge, theme);
        GeminiRequest request = buildRequest(prompt);

        try {
            GeminiResponse response = webClient.post()
                    .uri("/models/{model}:generateContent?key={key}", properties.model(), properties.key())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .blockOptional()
                    .orElseThrow(() -> new KidTaleException(ErrorCode.STORY_GENERATION_FAILED));

            return extractText(response);

        } catch (WebClientResponseException e) {
            log.error("[GeminiClient] generateContent failed. status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new KidTaleException(ErrorCode.STORY_GENERATION_FAILED);
        } catch (KidTaleException e) {
            throw e;
        } catch (Exception e) {
            log.error("[GeminiClient] generateContent failed.", e);
            throw new KidTaleException(ErrorCode.STORY_GENERATION_FAILED);
        }
    }

    private String buildPrompt(String childName, int childAge, String theme) {
        return STORY_PROMPT_TEMPLATE.formatted(childName, childAge, theme, childName, childAge);
    }

    private GeminiRequest buildRequest(String prompt) {
        GeminiPart part = new GeminiPart(prompt);
        GeminiContent content = new GeminiContent("user", List.of(part));
        GeminiGenerationConfig config = GeminiGenerationConfig.jsonMode();
        return new GeminiRequest(List.of(content), config);
    }

    private String extractText(GeminiResponse response) {
        List<GeminiCandidate> candidates = response.candidates();
        if (candidates == null || candidates.isEmpty()) {
            log.error("[GeminiClient] No candidates in response.");
            throw new KidTaleException(ErrorCode.STORY_GENERATION_FAILED);
        }

        List<GeminiPart> parts = candidates.get(0).content().parts();
        if (parts == null || parts.isEmpty()) {
            log.error("[GeminiClient] No parts in response content.");
            throw new KidTaleException(ErrorCode.STORY_GENERATION_FAILED);
        }

        return parts.get(0).text();
    }
}
