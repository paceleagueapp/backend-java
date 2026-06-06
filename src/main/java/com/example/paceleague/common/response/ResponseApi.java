package com.example.paceleague.common.response;

import java.time.Instant;

public record ResponseApi<T>(
        boolean success,
        String code,
        String message,
        T data,
        Instant timestamp
) {
    public static <T> ResponseApi<T> success(T data) {
        return new ResponseApi<>(
                true,
                "SUCCESS",
                "정상 처리되었습니다.",
                data,
                Instant.now()
        );
    }

    public static <T> ResponseApi<T> success(String message, T data) {
        return new ResponseApi<>(
                true,
                "SUCCESS",
                message,
                data,
                Instant.now()
        );
    }

    public static <T> ResponseApi<T> fail(String code, String message) {
        return new ResponseApi<>(
                false,
                code,
                message,
                null,
                Instant.now()
        );
    }
}
