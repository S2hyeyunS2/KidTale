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
    AI_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI_SERVICE_UNAVAILABLE", "AI 서비스가 일시적으로 과부하 상태입니다. 잠시 후 다시 시도해주세요."),

    // SweetBook / Book
    SWEETBOOK_API_ERROR(HttpStatus.BAD_GATEWAY, "SWEETBOOK_API_ERROR", "책 생성 중 오류가 발생했습니다."),
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOK_NOT_FOUND", "책을 찾을 수 없습니다."),
    BOOK_ALREADY_CREATED(HttpStatus.BAD_REQUEST, "BOOK_ALREADY_CREATED", "이미 책이 생성된 동화입니다."),
    SWEETBOOK_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "SWEETBOOK_NOT_CONFIGURED", "SweetBook 템플릿 설정이 필요합니다."),

    // Auth
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "USERNAME_ALREADY_EXISTS", "이미 사용 중인 아이디입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "이미 사용 중인 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "아이디 또는 비밀번호가 올바르지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요합니다."),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "주문을 찾을 수 없습니다."),
    ORDER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ORDER_FAILED", "주문 처리에 실패했습니다."),
    ORDER_NOT_BOOK_CREATED(HttpStatus.BAD_REQUEST, "ORDER_NOT_BOOK_CREATED", "책 생성이 완료된 동화만 주문할 수 있습니다."),
    ORDER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "ORDER_ALREADY_EXISTS", "이미 주문된 동화입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
