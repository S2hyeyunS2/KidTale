package com.hyeyuns2.kidtale.image;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/api/images")
public class ImageProxyController {

    private static final String BASE_URL = "https://image.pollinations.ai";
    private static final int TIMEOUT_SECONDS = 60;

    private final WebClient webClient = WebClient.builder()
            .baseUrl(BASE_URL)
            .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
            .build();

    @GetMapping("/generate")
    public ResponseEntity<byte[]> generate(
            @RequestParam("prompt") String prompt,
            @RequestParam(name = "seed", defaultValue = "1") int seed,
            @RequestParam(name = "width", defaultValue = "512") int width,
            @RequestParam(name = "height", defaultValue = "512") int height
    ) {
        String encodedPrompt = URLEncoder.encode(prompt, StandardCharsets.UTF_8);
        String uri = String.format("/prompt/%s?width=%d&height=%d&seed=%d&nologo=true",
                encodedPrompt, width, height, seed);

        log.debug("[ImageProxy] 이미지 요청. prompt='{}', seed={}", prompt, seed);

        try {
            byte[] imageData = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .block();

            if (imageData == null || imageData.length == 0) {
                log.warn("[ImageProxy] 빈 응답. prompt='{}'", prompt);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            }

            log.debug("[ImageProxy] 이미지 수신 완료. size={}bytes", imageData.length);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(imageData);

        } catch (Exception e) {
            log.warn("[ImageProxy] 이미지 생성 실패. prompt='{}', error={}", prompt, e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
}
