package com.hyeyuns2.kidtale.common.exception;

import lombok.Getter;

@Getter
public class KidTaleException extends RuntimeException {

    private final ErrorCode errorCode;

    public KidTaleException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
