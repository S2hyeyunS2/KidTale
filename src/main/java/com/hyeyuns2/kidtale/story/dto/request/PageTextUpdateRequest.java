package com.hyeyuns2.kidtale.story.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PageTextUpdateRequest(
        @NotBlank(message = "텍스트를 입력해주세요.")
        @Size(max = 2000, message = "텍스트는 2000자 이하여야 합니다.")
        String text
) {}
