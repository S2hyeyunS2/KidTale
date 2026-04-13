package com.hyeyuns2.kidtale.story.dto.response;

import com.hyeyuns2.kidtale.story.entity.StoryStatus;

import java.time.LocalDateTime;
import java.util.List;

public record StoryResponse(
        Long id,
        String title,
        List<StoryPage> pages,
        String childName,
        int childAge,
        String theme,
        StoryStatus status,
        LocalDateTime createdAt
) {}
