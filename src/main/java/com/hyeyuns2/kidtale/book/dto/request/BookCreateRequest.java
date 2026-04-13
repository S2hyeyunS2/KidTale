package com.hyeyuns2.kidtale.book.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookCreateRequest(

        @NotNull(message = "동화 ID를 입력해주세요.")
        Long storyId,

        @NotBlank(message = "표지 템플릿 UID를 선택해주세요.")
        String coverTemplateUid,

        @NotBlank(message = "내지 템플릿 UID를 선택해주세요.")
        String contentTemplateUid
) {}
