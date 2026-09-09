package com.paceleague.rank.application.port.out;

import com.paceleague.rank.domain.entity.Rank;

public interface RankRepositoryPort {
    Rank save(Rank rank);

    // 회원 탈퇴 시 점수 로그 일괄 삭제.
    void deleteAllByUno(Long uno);
}
