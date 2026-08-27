package com.example.paceleague.common.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException e) {

        log.warn("Bad request: {}", e.getMessage(), e);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of(ErrorCode.BAD_REQUEST, e.getMessage()));
    }

    // 존재하지 않는 경로 요청 — 아래 catch-all(Exception)에 걸리면 500이 되므로 먼저 404로 처리한다.
    // (운영에서 springdoc을 끈 뒤 /swagger-ui.html·/v3/api-docs 같은 경로가 여기로 들어온다.)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NoResourceFoundException e) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(ErrorCode.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception e) {

        log.error("Unhandled exception", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of(ErrorCode.INTERNAL_ERROR, "서버 오류"));
    }
}