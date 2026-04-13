package com.hyeyuns2.kidtale.external.sweetbook.dto.response;

public record Pagination(int total, int limit, int offset, boolean hasNext) {}
