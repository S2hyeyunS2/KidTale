package com.hyeyuns2.kidtale.book.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyeyuns2.kidtale.book.dto.request.BookCreateRequest;
import com.hyeyuns2.kidtale.book.dto.response.BookCreateResponse;
import com.hyeyuns2.kidtale.common.exception.ErrorCode;
import com.hyeyuns2.kidtale.common.exception.KidTaleException;
import com.hyeyuns2.kidtale.external.sweetbook.client.SweetBookClient;
import com.hyeyuns2.kidtale.external.sweetbook.config.SweetBookProperties;
import com.hyeyuns2.kidtale.external.sweetbook.dto.request.CreateBookRequest;
import com.hyeyuns2.kidtale.external.sweetbook.dto.request.CreateContentRequest;
import com.hyeyuns2.kidtale.external.sweetbook.dto.request.CreateCoverRequest;
import com.hyeyuns2.kidtale.external.sweetbook.dto.response.Template;
import com.hyeyuns2.kidtale.story.dto.response.StoryPage;
import com.hyeyuns2.kidtale.story.entity.Story;
import com.hyeyuns2.kidtale.story.entity.StoryStatus;
import com.hyeyuns2.kidtale.story.repository.StoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BookService {

    private static final String DEFAULT_BOOK_SPEC_UID = "SQUAREBOOK_HC";

    private final StoryRepository storyRepository;
    private final SweetBookClient sweetBookClient;
    private final SweetBookProperties sweetBookProperties;
    private final ObjectMapper objectMapper;

    public BookService(
            StoryRepository storyRepository,
            SweetBookClient sweetBookClient,
            SweetBookProperties sweetBookProperties,
            ObjectMapper objectMapper
    ) {
        this.storyRepository = storyRepository;
        this.sweetBookClient = sweetBookClient;
        this.sweetBookProperties = sweetBookProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * 사용 가능한 템플릿 목록을 조회한다.
     * 사용자가 표지/내지 스타일을 선택하기 위한 API.
     *
     * @param templateKind "cover" 또는 "content", null이면 전체 조회
     */
    public List<Template> getTemplates(String templateKind) {
        String bookSpecUid = resolveBookSpecUid();
        return sweetBookClient.getTemplates(bookSpecUid, templateKind);
    }

    /**
     * 사용자가 선택한 템플릿으로 책을 생성한다.
     * DRAFT → BOOK_CREATED 상태 전이.
     */
    @Transactional
    public BookCreateResponse createBook(BookCreateRequest request) {
        Story story = storyRepository.findById(request.storyId())
                .orElseThrow(() -> new KidTaleException(ErrorCode.STORY_NOT_FOUND));

        validateStoryStatus(story);

        List<StoryPage> pages = deserializePages(story.getPagesJson());

        log.info("[BookService] 책 생성 시작. storyId={}, title={}, pages={}",
                story.getId(), story.getTitle(), pages.size());

        // 1. POST /books — 빈 책 생성
        String bookSpecUid = resolveBookSpecUid();
        String sweetBookId = sweetBookClient.createBook(
                new CreateBookRequest(story.getTitle(), bookSpecUid));
        log.debug("[BookService] 책 생성 완료. sweetBookId={}", sweetBookId);

        // 2. POST /books/{id}/cover — 사용자가 선택한 표지 템플릿 등록
        String coverParams = buildCoverParams(story.getTitle(), story.getChildName());
        sweetBookClient.createCover(sweetBookId,
                new CreateCoverRequest(request.coverTemplateUid(), coverParams, null));
        log.debug("[BookService] 표지 등록 완료. sweetBookId={}, templateUid={}",
                sweetBookId, request.coverTemplateUid());

        // 3. POST /books/{id}/contents — 사용자가 선택한 내지 템플릿으로 페이지별 등록
        for (int i = 0; i < pages.size(); i++) {
            StoryPage page = pages.get(i);
            String contentParams = buildContentParams(page.text(), page.pageNumber());
            String breakBefore = (i == 0) ? null : "page";
            sweetBookClient.createContents(sweetBookId,
                    new CreateContentRequest(
                            request.contentTemplateUid(),
                            contentParams,
                            null,
                            breakBefore));
        }
        log.debug("[BookService] 내지 {}페이지 등록 완료. sweetBookId={}", pages.size(), sweetBookId);

        // 4. POST /books/{id}/finalization — 최종화
        sweetBookClient.finalizeBook(sweetBookId);
        log.debug("[BookService] 책 최종화 완료. sweetBookId={}", sweetBookId);

        // 5. Story 상태 업데이트 DRAFT → BOOK_CREATED
        story.updateStatusToBookCreated(sweetBookId);
        log.info("[BookService] 책 생성 완료. storyId={}, sweetBookId={}", story.getId(), sweetBookId);

        return new BookCreateResponse(sweetBookId, story.getId(), story.getTitle());
    }

    private String resolveBookSpecUid() {
        return StringUtils.hasText(sweetBookProperties.bookSpecUid())
                ? sweetBookProperties.bookSpecUid()
                : DEFAULT_BOOK_SPEC_UID;
    }

    private void validateStoryStatus(Story story) {
        if (story.getStatus() != StoryStatus.DRAFT) {
            log.warn("[BookService] 이미 책이 생성된 동화입니다. storyId={}, status={}",
                    story.getId(), story.getStatus());
            throw new KidTaleException(ErrorCode.BOOK_ALREADY_CREATED);
        }
    }

    private String buildCoverParams(String title, String childName) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "bookTitle", title,
                    "childName", childName
            ));
        } catch (JsonProcessingException e) {
            log.error("[BookService] 표지 파라미터 직렬화 실패.", e);
            throw new KidTaleException(ErrorCode.SWEETBOOK_API_ERROR);
        }
    }

    private String buildContentParams(String text, int pageNumber) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "text", text,
                    "pageNumber", pageNumber
            ));
        } catch (JsonProcessingException e) {
            log.error("[BookService] 내지 파라미터 직렬화 실패.", e);
            throw new KidTaleException(ErrorCode.SWEETBOOK_API_ERROR);
        }
    }

    private List<StoryPage> deserializePages(String pagesJson) {
        try {
            return objectMapper.readValue(
                    pagesJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, StoryPage.class)
            );
        } catch (JsonProcessingException e) {
            log.error("[BookService] 페이지 역직렬화 실패.", e);
            throw new KidTaleException(ErrorCode.STORY_NOT_FOUND);
        }
    }
}
