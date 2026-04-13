package com.hyeyuns2.kidtale.external.sweetbook.dto.response;

public record BookSpec(
        String bookSpecUid,
        String name,
        int innerTrimWidthMm,
        int innerTrimHeightMm,
        int pageMin,
        int pageMax,
        int pageIncrement,
        String coverType,
        String bindingType,
        long priceBase,
        long pricePerIncrement
) {}
