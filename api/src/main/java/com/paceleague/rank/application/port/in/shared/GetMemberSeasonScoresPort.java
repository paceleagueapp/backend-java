package com.paceleague.rank.application.port.in.shared;

import com.paceleague.rank.domain.enums.RankTier;

import java.util.Collection;
import java.util.Map;

// 여러 회원의 현재 시즌 점수/티어를 한 번에 조회하는 크로스 도메인 포트(크루원 랭킹 등).
// 점수가 없는 회원은 결과 Map에서 빠진다(호출측이 기본값 처리).
public interface GetMemberSeasonScoresPort {

    Map<Long, MemberSeasonScore> getCurrentSeasonScores(Collection<Long> memberSnos);

    record MemberSeasonScore(int totalScore, RankTier tier) {
    }
}
