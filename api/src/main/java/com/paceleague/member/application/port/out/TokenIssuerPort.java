package com.paceleague.member.application.port.out;

public interface TokenIssuerPort {
    String createAccessToken(long memberSno, String memberId);
}
