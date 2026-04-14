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
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class GeminiClient {

    /** 재시도 대상 HTTP 상태 코드 (일시적 과부하) */
    private static final Set<Integer> RETRYABLE_STATUS = Set.of(429, 503);
    /** 최대 재시도 횟수 */
    private static final int MAX_RETRIES = 3;
    /** 재시도 기본 대기 시간(ms) — 시도마다 선형 증가 (2s, 4s, 6s) */
    private static final long RETRY_BASE_DELAY_MS = 2_000L;

    private static final String STORY_PROMPT_TEMPLATE = """
            아이 이름이 '%s'이고 %d살인 아이를 위한 '%s' 테마의 한국어 동화를 만들어주세요.

            요구사항:
            - %s가 주인공이어야 합니다
            - %d살에게 적합한 수준의 내용
            - 긍정적이고 교훈적인 결말
            - 반드시 12장면으로 구성하세요 (책에서 한 펼침 = 왼쪽 그림 + 오른쪽 글)
            - 각 장면의 text는 5~7문장으로 풍부하게 써주세요. 장면 묘사, 인물의 감정, 대화를 자연스럽게 섞어주세요
            - 이야기 흐름: 도입(1~2장면) → 전개(3~9장면) → 절정(10~11장면) → 해결/마무리(12장면)
            - imageDescription은 해당 장면의 핵심 그림을 영어로 구체적으로 묘사하세요 (주인공 외모, 행동, 배경, 분위기 포함, 50단어 이내)

            반드시 아래 JSON 형식으로만 출력하세요 (다른 텍스트 없이):
            {
              "title": "동화 제목",
              "pages": [
                {"pageNumber": 1, "text": "장면 내용 (5~7문장)", "imageDescription": "scene description in English within 50 words"},
                {"pageNumber": 2, "text": "장면 내용 (5~7문장)", "imageDescription": "scene description in English within 50 words"},
                {"pageNumber": 3, "text": "장면 내용 (5~7문장)", "imageDescription": "scene description in English within 50 words"},
                {"pageNumber": 4, "text": "장면 내용 (5~7문장)", "imageDescription": "scene description in English within 50 words"},
                {"pageNumber": 5, "text": "장면 내용 (5~7문장)", "imageDescription": "scene description in English within 50 words"},
                {"pageNumber": 6, "text": "장면 내용 (5~7문장)", "imageDescription": "scene description in English within 50 words"},
                {"pageNumber": 7, "text": "장면 내용 (5~7문장)", "imageDescription": "scene description in English within 50 words"},
                {"pageNumber": 8, "text": "장면 내용 (5~7문장)", "imageDescription": "scene description in English within 50 words"},
                {"pageNumber": 9, "text": "장면 내용 (5~7문장)", "imageDescription": "scene description in English within 50 words"},
                {"pageNumber": 10, "text": "장면 내용 (5~7문장)", "imageDescription": "scene description in English within 50 words"},
                {"pageNumber": 11, "text": "장면 내용 (5~7문장)", "imageDescription": "scene description in English within 50 words"},
                {"pageNumber": 12, "text": "장면 내용 (5~7문장)", "imageDescription": "scene description in English within 50 words"}
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

        // 1차: 기본 모델로 재시도
        try {
            return generateWithRetry(properties.model(), request);
        } catch (KidTaleException e) {
            if (e.getErrorCode() != ErrorCode.AI_SERVICE_UNAVAILABLE) {
                throw e;
            }
        }

        // 2차: 폴백 모델로 재시도 (기본 모델과 다를 때만)
        String fallback = properties.fallbackModel();
        if (StringUtils.hasText(fallback) && !fallback.equals(properties.model())) {
            log.warn("[GeminiClient] 기본 모델 재시도 모두 실패. 폴백 모델로 전환. fallback={}", fallback);
            return generateWithRetry(fallback, request);
        }

        throw new KidTaleException(ErrorCode.AI_SERVICE_UNAVAILABLE);
    }

    /**
     * 지정 모델로 최대 {@value MAX_RETRIES}회 재시도한다.
     * 503 / 429는 {@value RETRY_BASE_DELAY_MS}ms × 시도 횟수만큼 대기 후 재시도.
     * 그 외 오류는 즉시 실패.
     */
    private String generateWithRetry(String model, GeminiRequest request) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                GeminiResponse response = webClient.post()
                        .uri("/models/{model}:generateContent?key={key}", model, properties.key())
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(GeminiResponse.class)
                        .blockOptional()
                        .orElseThrow(() -> new KidTaleException(ErrorCode.STORY_GENERATION_FAILED));

                if (attempt > 1) {
                    log.info("[GeminiClient] {}번째 시도에서 성공. model={}", attempt, model);
                }
                return extractText(response);

            } catch (WebClientResponseException e) {
                int status = e.getStatusCode().value();

                if (RETRYABLE_STATUS.contains(status) && attempt < MAX_RETRIES) {
                    long delay = RETRY_BASE_DELAY_MS * attempt;
                    log.warn("[GeminiClient] {} 응답 — {}ms 후 재시도 ({}/{}). model={}",
                            status, delay, attempt, MAX_RETRIES, model);
                    sleep(delay);
                } else if (RETRYABLE_STATUS.contains(status)) {
                    log.error("[GeminiClient] {} 응답 — 재시도 {}회 모두 실패. model={}",
                            status, MAX_RETRIES, model);
                    throw new KidTaleException(ErrorCode.AI_SERVICE_UNAVAILABLE);
                } else {
                    log.error("[GeminiClient] generateContent 실패. status={}, body={}",
                            status, e.getResponseBodyAsString(), e);
                    throw new KidTaleException(ErrorCode.STORY_GENERATION_FAILED);
                }

            } catch (KidTaleException e) {
                throw e;
            } catch (Exception e) {
                log.error("[GeminiClient] generateContent 예외 발생. model={}", model, e);
                throw new KidTaleException(ErrorCode.STORY_GENERATION_FAILED);
            }
        }
        throw new KidTaleException(ErrorCode.AI_SERVICE_UNAVAILABLE);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
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
