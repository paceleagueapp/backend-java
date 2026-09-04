package com.paceleague.member.application.port.out;

public interface RefreshTokenStorePort {
    String issue(long memberSno);

    void revoke(String refreshToken);

    // 조회와 폐기를 원자적으로 처리해, 같은 refresh token으로 동시에 두 번 reissue될 수 없게 한다.
    Long validateAndRevoke(String refreshToken);
}
