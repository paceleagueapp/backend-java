package com.example.paceleague.member.application.port.in;

import com.example.paceleague.member.application.dto.AuthTokenInfo;

public interface MemberAuthService {
    AuthTokenInfo join(String memberId, String rawPassword, String nickname, String email);

    AuthTokenInfo login(String memberId, String rawPassword);

    AuthTokenInfo reissue(String refreshToken);

    void logout(String refreshToken);
}
