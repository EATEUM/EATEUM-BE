package com.eateum.eateumbe.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 공통 예외 클래스
 */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
