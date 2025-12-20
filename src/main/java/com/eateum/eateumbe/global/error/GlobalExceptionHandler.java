package com.eateum.eateumbe.global.error;

import com.eateum.eateumbe.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 모든 실패 응답 클래스
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
