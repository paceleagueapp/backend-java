package com.paceleague.member.adapter.out.jwt;

import com.paceleague.common.security.jwt.JwtTokenProvider;
import com.paceleague.member.application.port.out.TokenIssuerPort;
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
