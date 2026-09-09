package com.paceleague.territory.application.port.in.shared;

// 회원 탈퇴 시 그 회원이 소유한 땅·헥사곤 매핑을 전부 삭제한다.
public interface PurgeMemberTerritoryPort {
    void purge(Long memberSno);
}
