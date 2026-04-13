package com.hyeyuns2.kidtale.book.dto.request;

import jakarta.validation.constraints.NotNull;

public record BookCreateRequest(

        @NotNull(message = "동화 ID를 입력해주세요.")
        Long storyId
) {}
