package com.hyeyuns2.kidtale.order.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OrderCreateRequest(

        @NotNull(message = "동화 ID를 입력해주세요.")
        Long storyId,

        @NotBlank(message = "수령인 이름을 입력해주세요.")
        @Size(max = 100, message = "수령인 이름은 100자 이하로 입력해주세요.")
        String recipientName,

        @NotBlank(message = "연락처를 입력해주세요.")
        @Pattern(regexp = "^[0-9\\-+]{1,20}$", message = "올바른 연락처 형식이 아닙니다.")
        String recipientPhone,

        @NotBlank(message = "우편번호를 입력해주세요.")
        @Size(max = 10, message = "우편번호는 10자 이하로 입력해주세요.")
        String postalCode,

        @NotBlank(message = "주소를 입력해주세요.")
        @Size(max = 200, message = "주소는 200자 이하로 입력해주세요.")
        String address1,

        @Size(max = 200, message = "상세주소는 200자 이하로 입력해주세요.")
        String address2,

        @Size(max = 200, message = "배송 메모는 200자 이하로 입력해주세요.")
        String memo,

        @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
        @Max(value = 100, message = "수량은 100개 이하이어야 합니다.")
        int quantity
) {}
