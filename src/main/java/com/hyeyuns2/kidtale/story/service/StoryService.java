package com.hyeyuns2.kidtale.story.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyeyuns2.kidtale.common.exception.ErrorCode;
import com.hyeyuns2.kidtale.common.exception.KidTaleException;
import com.hyeyuns2.kidtale.external.ai.client.GeminiClient;
import com.hyeyuns2.kidtale.story.dto.StoryGeneratedContent;
import com.hyeyuns2.kidtale.story.dto.request.StoryCreateRequest;
import com.hyeyuns2.kidtale.story.dto.response.StoryPage;
import com.hyeyuns2.kidtale.story.dto.response.StoryResponse;
import com.hyeyuns2.kidtale.story.entity.Story;
import com.hyeyuns2.kidtale.story.repository.StoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class StoryService {

    private final StoryRepository storyRepository;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public StoryService(StoryRepository storyRepository, GeminiClient geminiClient, ObjectMapper objectMapper) {
        this.storyRepository = storyRepository;
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public StoryResponse generateAndSave(StoryCreateRequest request, Long userId) {
        log.debug("[StoryService] 동화 생성 시작. childName={}, childAge={}, theme={}, userId={}",
                request.childName(), request.childAge(), request.theme(), userId);

        String rawJson = geminiClient.generateStory(request.childName(), request.childAge(), request.theme());
        StoryGeneratedContent generated = parseGeneratedContent(rawJson);

        String pagesJson = serializePages(generated.pages());
        Story story = Story.create(userId, request.childName(), request.childAge(), request.theme(),
                generated.title(), pagesJson);
        Story saved = storyRepository.save(story);

        log.debug("[StoryService] 동화 저장 완료. id={}, title={}", saved.getId(), saved.getTitle());
        return toResponse(saved, generated.pages());
    }

    @Transactional(readOnly = true)
    public List<StoryResponse> findAll() {
        return storyRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(story -> toResponse(story, deserializePages(story.getPagesJson())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StoryResponse> findByUserId(Long userId) {
        return storyRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(story -> toResponse(story, deserializePages(story.getPagesJson())))
                .toList();
    }

    @Transactional
    public StoryResponse updatePageText(Long storyId, int pageNumber, String newText) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new KidTaleException(ErrorCode.STORY_NOT_FOUND));

        List<StoryPage> pages = deserializePages(story.getPagesJson());

        // pageNumber는 1-based
        boolean found = false;
        List<StoryPage> updatedPages = new java.util.ArrayList<>();
        for (StoryPage page : pages) {
            if (page.pageNumber() == pageNumber) {
                updatedPages.add(new StoryPage(page.pageNumber(), newText, page.imageDescription()));
                found = true;
            } else {
                updatedPages.add(page);
            }
        }

        if (!found) {
            throw new KidTaleException(ErrorCode.STORY_NOT_FOUND);
        }

        story.updatePagesJson(serializePages(updatedPages));
        log.debug("[StoryService] 페이지 텍스트 수정 완료. storyId={}, pageNumber={}", storyId, pageNumber);

        return toResponse(story, updatedPages);
    }

    @Transactional(readOnly = true)
    public StoryResponse findById(Long id) {
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new KidTaleException(ErrorCode.STORY_NOT_FOUND));
        List<StoryPage> pages = deserializePages(story.getPagesJson());
        return toResponse(story, pages);
    }

    private StoryGeneratedContent parseGeneratedContent(String rawJson) {
        String cleaned = stripMarkdownWrapper(rawJson);
        try {
            return objectMapper.readValue(cleaned, StoryGeneratedContent.class);
        } catch (JsonProcessingException e) {
            log.error("[StoryService] Gemini 응답 JSON 파싱 실패. raw={}", rawJson, e);
            throw new KidTaleException(ErrorCode.STORY_GENERATION_FAILED);
        }
    }

    private String serializePages(List<StoryPage> pages) {
        try {
            return objectMapper.writeValueAsString(pages);
        } catch (JsonProcessingException e) {
            log.error("[StoryService] 페이지 직렬화 실패.", e);
            throw new KidTaleException(ErrorCode.STORY_GENERATION_FAILED);
        }
    }

    List<StoryPage> deserializePages(String pagesJson) {
        try {
            return objectMapper.readValue(
                    pagesJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, StoryPage.class)
            );
        } catch (JsonProcessingException e) {
            log.error("[StoryService] 페이지 역직렬화 실패.", e);
            throw new KidTaleException(ErrorCode.STORY_NOT_FOUND);
        }
    }

    private String stripMarkdownWrapper(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
            int end = trimmed.lastIndexOf("```");
            if (end > 0) {
                trimmed = trimmed.substring(0, end);
            }
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
            int end = trimmed.lastIndexOf("```");
            if (end > 0) {
                trimmed = trimmed.substring(0, end);
            }
        }
        return trimmed.trim();
    }

    private StoryResponse toResponse(Story story, List<StoryPage> pages) {
        return new StoryResponse(
                story.getId(),
                story.getTitle(),
                pages,
                story.getChildName(),
                story.getChildAge(),
                story.getTheme(),
                story.getStatus(),
                story.getCreatedAt()
        );
    }
}
