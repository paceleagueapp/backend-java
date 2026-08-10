package com.example.paceleague.common.web;

import com.example.paceleague.common.security.JwtAuthenticationFilter;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class MemberSnoArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(MemberSno.class)
                && (parameter.getParameterType() == Long.class || parameter.getParameterType() == long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                   NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        MemberSno annotation = parameter.getParameterAnnotation(MemberSno.class);
        boolean required = annotation == null || annotation.required();

        Authentication authentication = (Authentication) webRequest.getUserPrincipal();

        if (!required) {
            if (authentication == null || !(authentication.getPrincipal() instanceof JwtAuthenticationFilter.AuthPrincipal p)) {
                return null;
            }
            return p.memberSno();
        }

        // required=true일 때 인증 정보가 없으면 기존 uno(authentication)와 동일하게 예외를 그대로 던져
        // GlobalExceptionHandler의 일반 500 처리로 흐르게 한다 — 상태코드를 임의로 바꾸지 않는다.
        var p = (JwtAuthenticationFilter.AuthPrincipal) authentication.getPrincipal();
        return p.memberSno();
    }
}
