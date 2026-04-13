package com.hyeyuns2.kidtale.common.init;

import com.hyeyuns2.kidtale.story.dto.response.StoryPage;

import java.util.List;

/**
 * stories.json 파싱용 내부 DTO
 */
public record DummyStoryData(
        String childName,
        int childAge,
        String theme,
        String title,
        List<StoryPage> pages
) {}
