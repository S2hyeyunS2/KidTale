package com.hyeyuns2.kidtale.external.sweetbook.dto.request;

import java.util.Map;

/**
 * POST /books/{bookUid}/contents — multipart/form-data
 *
 * @param templateUid  SweetBook 내지 템플릿 UID
 * @param parameters   템플릿 변수 (JSON 문자열)
 * @param images       템플릿 변수명 → 이미지 바이너리 (nullable)
 * @param breakBefore  페이지 레이아웃 제어: "page" | "column" | "none" (nullable)
 */
public record CreateContentRequest(
        String templateUid,
        String parameters,
        Map<String, byte[]> images,
        String breakBefore
) {}
