package com.hyeyuns2.kidtale.story.dto.response;

public record StoryPage(
        int pageNumber,
        String text,
        String imageDescription
) {}
