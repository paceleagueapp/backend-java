package com.example.paceleague.member.adapter.out.jwt;

import com.example.paceleague.common.security.jwt.JwtTokenProvider;
import com.example.paceleague.member.application.port.out.TokenIssuerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenAdapter implements TokenIssuerPort {

    private final JwtTokenProvider jwtTokenProvider;

    public String createAccessToken(long memberSno, String memberId) {
        return jwtTokenProvider.createAccessToken(memberSno, memberId);
    }
}
