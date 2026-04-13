package com.hyeyuns2.kidtale.external.sweetbook.dto.response;

public record Template(
        String templateUid,
        String templateName,
        String templateKind,   // cover | content | divider | publish
        String category,
        String theme,
        String bookSpecUid,
        boolean isPublic,
        String status
) {}
