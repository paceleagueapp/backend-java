package com.paceleague.common.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// MemberSno와 같은 목적(캐스팅 반복 제거)이지만 principal 타입이 다르다 —
// AdminSessionFilter.AdminPrincipal 인지 확인 후 adminSno를 꺼낸다.
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminSno {
    boolean required() default true;
}
