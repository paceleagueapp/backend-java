package com.paceleague.common.web;

import com.paceleague.common.security.AdminSessionFilter;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AdminSnoArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AdminSno.class)
                && (parameter.getParameterType() == Long.class || parameter.getParameterType() == long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                   NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        AdminSno annotation = parameter.getParameterAnnotation(AdminSno.class);
        boolean required = annotation == null || annotation.required();

        Authentication authentication = (Authentication) webRequest.getUserPrincipal();

        if (!required) {
            if (authentication == null || !(authentication.getPrincipal() instanceof AdminSessionFilter.AdminPrincipal p)) {
                return null;
            }
            return p.adminSno();
        }

        var p = (AdminSessionFilter.AdminPrincipal) authentication.getPrincipal();
        return p.adminSno();
    }
}
