package com.hyeyuns2.kidtale.external.sweetbook.dto.response;

public record SweetBookApiResponse<T>(
        boolean success,
        String message,
        T data
) {}
