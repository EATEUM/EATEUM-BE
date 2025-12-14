package com.eateum.eateumbe.global.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    // 성공 응답 (데이터 O)
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "성공", data);
    }

    // 실패 응답
    public static<T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }

}
