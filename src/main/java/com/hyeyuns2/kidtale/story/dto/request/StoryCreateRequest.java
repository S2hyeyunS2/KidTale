package com.hyeyuns2.kidtale.story.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StoryCreateRequest(

        @NotBlank(message = "아이 이름을 입력해주세요.")
        String childName,

        @NotNull(message = "아이 나이를 입력해주세요.")
        @Min(value = 1, message = "나이는 1살 이상이어야 합니다.")
        @Max(value = 12, message = "나이는 12살 이하이어야 합니다.")
        Integer childAge,

        @NotBlank(message = "테마를 입력해주세요.")
        String theme
) {}
