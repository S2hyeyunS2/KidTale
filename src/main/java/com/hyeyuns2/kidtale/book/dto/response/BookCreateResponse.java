package com.hyeyuns2.kidtale.book.dto.response;

public record BookCreateResponse(
        String sweetBookId,
        Long storyId,
        String title
) {}
