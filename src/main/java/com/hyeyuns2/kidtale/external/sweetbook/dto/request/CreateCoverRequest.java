package com.hyeyuns2.kidtale.external.sweetbook.dto.request;

import java.util.Map;

/**
 * POST /books/{bookUid}/cover — multipart/form-data
 *
 * @param templateUid  SweetBook 커버 템플릿 UID
 * @param parameters   템플릿 변수 (JSON 문자열, e.g. {"title":"My Book"})
 * @param images       템플릿 변수명 → 이미지 바이너리 (nullable)
 */
public record CreateCoverRequest(
        String templateUid,
        String parameters,
        Map<String, byte[]> images
) {}
