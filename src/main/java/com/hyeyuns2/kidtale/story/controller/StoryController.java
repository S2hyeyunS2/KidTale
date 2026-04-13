package com.hyeyuns2.kidtale.story.controller;

import com.hyeyuns2.kidtale.common.response.ApiResponse;
import com.hyeyuns2.kidtale.story.dto.request.StoryCreateRequest;
import com.hyeyuns2.kidtale.story.dto.response.StoryResponse;
import com.hyeyuns2.kidtale.story.service.StoryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/stories")
public class StoryController {

    private final StoryService storyService;

    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StoryResponse>> generateStory(
            @Valid @RequestBody StoryCreateRequest request
    ) {
        log.debug("[StoryController] POST /api/stories. childName={}", request.childName());
        StoryResponse response = storyService.generateAndSave(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("동화가 생성되었습니다.", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StoryResponse>> getStory(@PathVariable Long id) {
        log.debug("[StoryController] GET /api/stories/{}", id);
        StoryResponse response = storyService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
