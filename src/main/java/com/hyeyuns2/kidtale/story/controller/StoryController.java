package com.hyeyuns2.kidtale.story.controller;

import com.hyeyuns2.kidtale.common.response.ApiResponse;
import com.hyeyuns2.kidtale.common.security.SecurityUtils;
import com.hyeyuns2.kidtale.story.dto.request.PageTextUpdateRequest;
import com.hyeyuns2.kidtale.story.dto.request.StoryCreateRequest;
import com.hyeyuns2.kidtale.story.dto.response.StoryResponse;
import com.hyeyuns2.kidtale.story.service.StoryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/stories")
public class StoryController {

    private final StoryService storyService;
    private final SecurityUtils securityUtils;

    public StoryController(StoryService storyService, SecurityUtils securityUtils) {
        this.storyService = storyService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StoryResponse>>> getStories() {
        log.debug("[StoryController] GET /api/stories");
        List<StoryResponse> response = storyService.findAll();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 내 동화 목록 — 로그인 필요 */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<StoryResponse>>> getMyStories(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = securityUtils.resolveUserIdOrNull(userDetails);
        log.debug("[StoryController] GET /api/stories/my. userId={}", userId);
        List<StoryResponse> response = storyService.findByUserId(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StoryResponse>> generateStory(
            @Valid @RequestBody StoryCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = securityUtils.resolveUserIdOrNull(userDetails);
        log.debug("[StoryController] POST /api/stories. childName={}, userId={}", request.childName(), userId);
        StoryResponse response = storyService.generateAndSave(request, userId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("동화가 생성되었습니다.", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StoryResponse>> getStory(@PathVariable("id") Long id) {
        log.debug("[StoryController] GET /api/stories/{}", id);
        StoryResponse response = storyService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 페이지 텍스트 수정 */
    @PatchMapping("/{id}/pages/{pageNumber}")
    public ResponseEntity<ApiResponse<StoryResponse>> updatePageText(
            @PathVariable("id") Long id,
            @PathVariable("pageNumber") int pageNumber,
            @Valid @RequestBody PageTextUpdateRequest request
    ) {
        log.debug("[StoryController] PATCH /api/stories/{}/pages/{}. text={}", id, pageNumber, request.text());
        StoryResponse response = storyService.updatePageText(id, pageNumber, request.text());
        return ResponseEntity.ok(ApiResponse.ok("페이지가 수정되었습니다.", response));
    }
}
