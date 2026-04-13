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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BookService {

    private static final String DEFAULT_BOOK_SPEC_UID = "SQUAREBOOK_HC";
    private static final String POLLINATIONS_BASE = "https://image.pollinations.ai/prompt/";

    // 날짜·알림장 계열 템플릿은 동화책 용도에 부적합하므로 자동 선택 시 제외
    private static final List<String> EXCLUDED_TEMPLATE_KEYWORDS =
            List.of("알림장", "월시작", "dateA", "dateB", "monthHeader", "date");

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

    /** 템플릿 목록 조회 (어드민/참고용) */
    public List<Template> getTemplates(String templateKind) {
        return sweetBookClient.getTemplates(resolveBookSpecUid(), templateKind);
    }

    /**
     * 동화 ID만 받아 책을 자동으로 생성한다.
     * 템플릿은 SweetBook API에서 조회한 뒤 자동 선택한다.
     * DRAFT → BOOK_CREATED 상태 전이.
     */
    @Transactional
    public BookCreateResponse createBook(BookCreateRequest request) {
        Story story = storyRepository.findById(request.storyId())
                .orElseThrow(() -> new KidTaleException(ErrorCode.STORY_NOT_FOUND));

        if (story.getStatus() != StoryStatus.DRAFT) {
            log.warn("[BookService] 이미 책이 생성된 동화입니다. storyId={}", story.getId());
            throw new KidTaleException(ErrorCode.BOOK_ALREADY_CREATED);
        }

        List<StoryPage> pages = deserializePages(story.getPagesJson());

        log.info("[BookService] 책 생성 시작. storyId={}, title={}, pages={}",
                story.getId(), story.getTitle(), pages.size());

        // 템플릿 자동 선택
        String coverTemplateUid   = selectTemplate("cover");
        String contentTemplateUid = selectTemplate("content");
        log.debug("[BookService] 선택된 템플릿. cover={}, content={}", coverTemplateUid, contentTemplateUid);

        // 1. POST /books
        String sweetBookId = sweetBookClient.createBook(
                new CreateBookRequest(story.getTitle(), resolveBookSpecUid()));

        // 2. POST /books/{id}/cover
        sweetBookClient.createCover(sweetBookId,
                new CreateCoverRequest(
                        coverTemplateUid,
                        buildCoverParams(story.getTitle(), story.getChildName(), story.getTheme()),
                        null));

        // 3. POST /books/{id}/contents (페이지별)
        for (int i = 0; i < pages.size(); i++) {
            StoryPage page = pages.get(i);
            sweetBookClient.createContents(sweetBookId,
                    new CreateContentRequest(
                            contentTemplateUid,
                            buildContentParams(page.text(), page.pageNumber(), page.imageDescription()),
                            null,
                            i == 0 ? null : "page"));
        }

        // 4. POST /books/{id}/finalization
        sweetBookClient.finalizeBook(sweetBookId);

        story.updateStatusToBookCreated(sweetBookId);
        log.info("[BookService] 책 생성 완료. storyId={}, sweetBookId={}", story.getId(), sweetBookId);

        return new BookCreateResponse(sweetBookId, story.getId(), story.getTitle());
    }

    // ── 템플릿 자동 선택 ──────────────────────────────────────────────────────

    /**
     * templateKind("cover" | "content")에 맞는 템플릿을 자동으로 선택한다.
     * 우선순위: 빈내지/내지 단순 템플릿 > 제외 키워드 없는 첫 번째 템플릿 > 어떤 것이든 첫 번째
     */
    private String selectTemplate(String templateKind) {
        List<Template> templates;
        try {
            templates = sweetBookClient.getTemplates(resolveBookSpecUid(), templateKind);
        } catch (Exception e) {
            log.warn("[BookService] 템플릿 조회 실패, 기본값 사용. kind={}", templateKind, e);
            throw new KidTaleException(ErrorCode.SWEETBOOK_API_ERROR);
        }

        if (templates == null || templates.isEmpty()) {
            throw new KidTaleException(ErrorCode.SWEETBOOK_NOT_CONFIGURED);
        }

        if ("content".equals(templateKind)) {
            // 빈내지 우선 (이미지·날짜 파라미터 불필요)
            return templates.stream()
                    .filter(t -> t.templateName().equals("빈내지"))
                    .map(Template::templateUid)
                    .findFirst()
                    // 내지, 내지a, 내지b 등 단순 텍스트 내지 우선
                    .orElseGet(() -> templates.stream()
                            .filter(t -> t.templateName().matches("내지[ab]?"))
                            .map(Template::templateUid)
                            .findFirst()
                            .orElseGet(() -> pickFirstExcluding(templates)));
        }

        // cover: 제외 키워드 없는 첫 번째
        return pickFirstExcluding(templates);
    }

    private String pickFirstExcluding(List<Template> templates) {
        return templates.stream()
                .filter(t -> EXCLUDED_TEMPLATE_KEYWORDS.stream()
                        .noneMatch(k -> t.templateName().contains(k)))
                .map(Template::templateUid)
                .findFirst()
                .orElse(templates.get(0).templateUid()); // 모두 제외되면 그냥 첫 번째
    }

    // ── 파라미터 빌더 ─────────────────────────────────────────────────────────

    /**
     * 표지 파라미터.
     * 템플릿마다 요구하는 필드 이름이 달라 알려진 모든 이름을 채워 넣는다.
     */
    private String buildCoverParams(String title, String childName, String theme) {
        String imageUrl  = buildCoverImageUrl(title, theme);
        String year      = String.valueOf(LocalDate.now().getYear());
        String dateRange = year + ".01 - " + year + ".12";

        Map<String, Object> params = new LinkedHashMap<>();
        // 텍스트 파라미터 (템플릿마다 이름 다름)
        params.put("bookTitle",   title);
        params.put("title",       title);
        params.put("titleText",   title);
        params.put("spineTitle",  title);
        params.put("childName",   childName);
        params.put("name",        childName);
        params.put("subtitle",    theme);
        params.put("description", theme + " 동화");
        params.put("dateRange",   dateRange);
        params.put("date",        year);
        params.put("year",        year);
        params.put("text",        title);
        // 이미지 파라미터
        params.put("coverPhoto",  imageUrl);
        params.put("frontPhoto",  imageUrl);
        params.put("backPhoto",   imageUrl);
        params.put("spinePhoto",  imageUrl);
        params.put("coverImage",  imageUrl);
        params.put("mainPhoto",   imageUrl);
        params.put("photo",       imageUrl);
        params.put("image",       imageUrl);
        return serialize(params);
    }

    /**
     * 내지 파라미터.
     * imageDescription으로 Pollinations.ai AI 삽화 URL을 생성해 주입한다.
     */
    private String buildContentParams(String text, int pageNumber, String imageDescription) {
        String imageUrl  = buildContentImageUrl(imageDescription, pageNumber);
        String year      = String.valueOf(LocalDate.now().getYear());
        String dateRange = year + ".01 - " + year + ".12";

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("text",          text);
        params.put("pageText",      text);
        params.put("pageNumber",    pageNumber);
        params.put("title",         "");
        params.put("dateRange",     dateRange);
        params.put("date",          year);
        params.put("contentPhoto",  imageUrl);
        params.put("pagePhoto",     imageUrl);
        params.put("contentImage",  imageUrl);
        params.put("photo",         imageUrl);
        params.put("image",         imageUrl);
        params.put("mainPhoto",     imageUrl);
        return serialize(params);
    }

    private String serialize(Map<String, Object> params) {
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            log.error("[BookService] 파라미터 직렬화 실패.", e);
            throw new KidTaleException(ErrorCode.SWEETBOOK_API_ERROR);
        }
    }

    // ── 이미지 URL 빌더 (Pollinations.ai) ────────────────────────────────────

    private String buildCoverImageUrl(String title, String theme) {
        String desc = title + " " + theme + " children fairy tale book cover illustration colorful cute";
        return POLLINATIONS_BASE + urlEncode(desc) + "?width=800&height=800&model=flux&nologo=true";
    }

    private String buildContentImageUrl(String imageDescription, int pageNumber) {
        String base = StringUtils.hasText(imageDescription)
                ? imageDescription
                : "fairy tale scene page " + pageNumber;
        String desc = base + " children book illustration watercolor colorful";
        return POLLINATIONS_BASE + urlEncode(desc)
                + "?width=800&height=600&model=flux&nologo=true&seed=" + pageNumber;
    }

    private String urlEncode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    private String resolveBookSpecUid() {
        return StringUtils.hasText(sweetBookProperties.bookSpecUid())
                ? sweetBookProperties.bookSpecUid()
                : DEFAULT_BOOK_SPEC_UID;
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
