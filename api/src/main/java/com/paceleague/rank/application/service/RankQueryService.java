package com.paceleague.rank.application.service;

import com.paceleague.common.i18n.Language;
import com.paceleague.rank.application.dto.RankMeResponse;
import com.paceleague.rank.application.port.in.shared.GetMemberSeasonScoresPort;
import com.paceleague.rank.application.port.in.shared.GetMemberTierPort;
import com.paceleague.rank.application.port.in.GetMyRankUseCase;
import com.paceleague.rank.application.port.out.MemberScoreRepositoryPort;
import com.paceleague.rank.domain.entity.MemberScore;
import com.paceleague.rank.domain.enums.RankTier;
import com.paceleague.rank.domain.policy.RankTierLabelPolicy;
import com.paceleague.season.application.port.in.shared.GetCurrentSeasonPort;
import com.paceleague.season.domain.entity.Season;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankQueryService implements GetMyRankUseCase, GetMemberTierPort, GetMemberSeasonScoresPort {

    private static final int DEFAULT_SCORE = 1500;
    private static final RankTier DEFAULT_TIER = RankTier.SILVER;

    private final MemberScoreRepositoryPort memberScoreRepositoryPort;
    private final GetCurrentSeasonPort getCurrentSeasonPort;

    public RankMeResponse getMyRank(Long memberSno, String lang) {
        Language language = Language.fromCode(lang);

        MemberScore memberScore = findMemberScore(memberSno).orElse(null);
        int totalScore = memberScore == null ? DEFAULT_SCORE : memberScore.getTotalScore();
        RankTier currentTier = memberScore == null ? DEFAULT_TIER : memberScore.getTier();
        RankTier nextTier = currentTier.next();

        int nextTierRequiredScore = nextTier == null ? 0 : nextTier.getMinScore();
        int remainingScore = nextTier == null ? 0 : nextTierRequiredScore - totalScore;

        return new RankMeResponse(
                totalScore,
                currentTier,
                RankTierLabelPolicy.label(currentTier, language),
                nextTier,
                nextTier == null ? null : RankTierLabelPolicy.label(nextTier, language),
                nextTierRequiredScore,
                remainingScore
        );
    }

    public RankTier getTier(Long memberSno) {
        return findMemberScore(memberSno).map(MemberScore::getTier).orElse(DEFAULT_TIER);
    }

    public Map<Long, MemberSeasonScore> getCurrentSeasonScores(Collection<Long> memberSnos) {
        if (memberSnos == null || memberSnos.isEmpty()) {
            return Map.of();
        }
        Season season = getCurrentSeasonPort.getCurrentSeason();
        return memberScoreRepositoryPort.findByMemberSnosAndSeasonSno(memberSnos, season.getSeason())
                .stream()
                .collect(Collectors.toMap(
                        MemberScore::getMemberSno,
                        ms -> new MemberSeasonScore(ms.getTotalScore(), ms.getTier()),
                        (a, b) -> a));
    }

    private Optional<MemberScore> findMemberScore(Long memberSno) {
        Season season = getCurrentSeasonPort.getCurrentSeason();

        // ranking 쪽(RankingQueryService)은 season.getSno()로 조회하는 것과 달리 여기는 season.getSeason()을 그대로 사용 —
        // 기존부터 있던 두 조회 방식의 불일치이며, 이번 리팩토링에서 의도적으로 고치지 않고 보존함.
        return memberScoreRepositoryPort.findByMemberSnoAndSeasonSno(memberSno, season.getSeason());
    }
}
