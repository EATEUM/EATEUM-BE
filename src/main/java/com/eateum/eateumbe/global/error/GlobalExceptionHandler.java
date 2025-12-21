package com.eateum.eateumbe.global.error;

import com.eateum.eateumbe.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * [전역 예외 처리기] : Controller / Service 계층에서 발생하는 예외를 처리
 *
 * - ApiException을 공통 응답 포맷(ApiResponse)으로 변환
 */
@Slf4j
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    //비즈니스 예외 (직접 던짐)
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(ApiResponse.fail(e.getMessage()));
    }

    //DB UNIQUE 제약 조건 위반
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("DuplicateKeyException 발생", e);
        String message = "이미 사용 중인 값입니다.";

        if(e.getMessage() != null) {
            if(e.getMessage().contains("uk_users_email")) {
                message = "이미 사용 중인 이메일입니다.";
            } else if(e.getMessage().contains("uk_users_phone")) {
                message = "이미 사용 중인 전화번호입니다.";
            }
        }

        return  ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(message));
    }

    //파일 업로드 예외
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException e
    ) {
        log.warn("File upload size exceeded", e);
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail("파일 용량은 5MB 이하만 가능합니다."));
    }

    //Validation 실패 처리 (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException e
    ) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .get(0)
                .getDefaultMessage();

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(message));
    }

    //그 외 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity
                .status(500)
                .body(ApiResponse.fail("서버 오류가 발생했습니다."));
    }
}
