package com.hyeyuns2.kidtale.external.sweetbook.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateBookRequest(
        String title,
        String bookSpecUid,
        String specProfileUid,
        String externalRef
) {
    public CreateBookRequest(String title, String bookSpecUid) {
        this(title, bookSpecUid, null, null);
    }
}
