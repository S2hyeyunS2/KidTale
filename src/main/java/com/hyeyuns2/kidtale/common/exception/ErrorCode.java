package com.hyeyuns2.kidtale.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청입니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 오류가 발생했습니다."),

    // Story
    STORY_NOT_FOUND(HttpStatus.NOT_FOUND, "STORY_NOT_FOUND", "동화를 찾을 수 없습니다."),
    STORY_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "STORY_GENERATION_FAILED", "동화 생성에 실패했습니다."),

    // SweetBook / Book
    SWEETBOOK_API_ERROR(HttpStatus.BAD_GATEWAY, "SWEETBOOK_API_ERROR", "책 생성 중 오류가 발생했습니다."),
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOK_NOT_FOUND", "책을 찾을 수 없습니다."),
    BOOK_ALREADY_CREATED(HttpStatus.BAD_REQUEST, "BOOK_ALREADY_CREATED", "이미 책이 생성된 동화입니다."),
    SWEETBOOK_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "SWEETBOOK_NOT_CONFIGURED", "SweetBook 템플릿 설정이 필요합니다."),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "주문을 찾을 수 없습니다."),
    ORDER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ORDER_FAILED", "주문 처리에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
