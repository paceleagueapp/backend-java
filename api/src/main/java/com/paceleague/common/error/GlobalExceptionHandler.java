package com.paceleague.common.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
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

    // 쿼리/경로 파라미터 바인딩 실패(타입 불일치 예: zoom=16.5 → int, 필수 파라미터 누락) — 클라이언트 잘못이므로
    // catch-all의 500이 아니라 400으로 내린다. (도메인 검증 실패를 IllegalArgumentException→400으로 처리하는 것과 같은 취지.)
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiError> handleBadParam(Exception e) {

        log.warn("Bad request param: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of(ErrorCode.BAD_REQUEST, "요청 파라미터가 올바르지 않습니다."));
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