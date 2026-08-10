package com.example.paceleague.common.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 인증된 컨트롤러마다 반복되던 `((AuthPrincipal) authentication.getPrincipal()).memberSno()` 캐스팅을
// 한 곳으로 모은 것 — required=false는 board 조회(GET)처럼 비로그인도 허용하는 엔드포인트에서 null을 받기 위함.
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface MemberSno {
    boolean required() default true;
}
