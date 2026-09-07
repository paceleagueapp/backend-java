package com.paceleague.common.error;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void 잘못된_파라미터는_500이_아니라_400으로_내려간다() {
        // 2026-09-07 회귀: zoom=16.5 처럼 int 파라미터에 소수가 오면 예전엔 catch-all의 500이었다.
        ResponseEntity<ApiError> response =
                handler.handleBadParam(new MissingServletRequestParameterException("zoom", "int"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(ErrorCode.BAD_REQUEST);
    }

    @Test
    void 파라미터_오류_응답은_예외_내부_메시지를_노출하지_않는다() {
        ResponseEntity<ApiError> response =
                handler.handleBadParam(new MissingServletRequestParameterException("token", "String"));

        // 파라미터명("token") 등 내부 정보가 그대로 새지 않고 고정 문구만 내려가야 한다.
        assertThat(response.getBody().message()).isEqualTo("요청 파라미터가 올바르지 않습니다.");
        assertThat(response.getBody().message()).doesNotContain("token");
    }

    @Test
    void IllegalArgumentException은_그대로_400이다() {
        ResponseEntity<ApiError> response =
                handler.handleBadRequest(new IllegalArgumentException("이미 가입된 아이디입니다"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("이미 가입된 아이디입니다");
    }
}
