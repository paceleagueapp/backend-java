package com.paceleague.member.application.port.in;

import com.paceleague.member.application.dto.AuthTokenInfo;

public interface MemberAuthUseCase {
    AuthTokenInfo join(String memberId, String rawPassword, String nickname, String email);

    AuthTokenInfo login(String memberId, String rawPassword);

    AuthTokenInfo reissue(String refreshToken);

    void logout(String refreshToken);
}
