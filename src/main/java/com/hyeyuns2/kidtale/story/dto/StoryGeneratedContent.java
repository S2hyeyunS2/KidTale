package com.hyeyuns2.kidtale.story.dto;

import com.hyeyuns2.kidtale.story.dto.response.StoryPage;

import java.util.List;

/**
 * Gemini API 응답 JSON을 파싱하기 위한 내부 DTO.
 */
public record StoryGeneratedContent(String title, List<StoryPage> pages) {}
