package com.example.paceleague.crew.application.service;

import com.example.paceleague.common.i18n.Language;
import com.example.paceleague.crew.application.dto.CrewDetailResponse;
import com.example.paceleague.crew.application.dto.CrewMemberResponse;
import com.example.paceleague.crew.application.dto.CrewRankingEntryResponse;
import com.example.paceleague.crew.application.dto.CrewSummaryResponse;
import com.example.paceleague.crew.application.port.in.CrewQueryUseCase;
import com.example.paceleague.crew.application.port.in.GetMemberCrewBadgePort;
import com.example.paceleague.crew.application.port.out.CrewMemberRepositoryPort;
import com.example.paceleague.crew.application.port.out.CrewRepositoryPort;
import com.example.paceleague.crew.config.CrewProperties;
import com.example.paceleague.crew.domain.entity.Crew;
import com.example.paceleague.crew.domain.entity.CrewMember;
import com.example.paceleague.member.application.port.in.GetMemberNicknamePort;
import com.example.paceleague.rank.application.port.in.GetMemberSeasonScoresPort;
import com.example.paceleague.rank.application.port.in.GetMemberSeasonScoresPort.MemberSeasonScore;
import com.example.paceleague.rank.application.port.in.GetMemberTierPort;
import com.example.paceleague.rank.domain.enums.RankTier;
import com.example.paceleague.rank.domain.policy.RankTierLabelPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CrewQueryServiceImpl implements CrewQueryUseCase, GetMemberCrewBadgePort {

    private static final int DEFAULT_SCORE = 1500;
    private static final RankTier DEFAULT_TIER = RankTier.SILVER;

    private final CrewRepositoryPort crewRepositoryPort;
    private final CrewMemberRepositoryPort crewMemberRepositoryPort;
    private final GetMemberNicknamePort getMemberNicknamePort;
    private final GetMemberTierPort getMemberTierPort;
    private final GetMemberSeasonScoresPort getMemberSeasonScoresPort;
    private final CrewProperties props;

    @Override
    public List<CrewSummaryResponse> search(String q, String lang) {
        return crewRepositoryPort.searchActiveByName(q, props.searchMaxResults()).stream()
                .map(c -> new CrewSummaryResponse(
                        c.getSno(), c.getName(), c.getIconUrl(), c.getDescription(),
                        c.getMemberCount(), c.getMemberLimit(), c.getJoinPolicy()))
                .toList();
    }

    @Override
    public CrewDetailResponse getDetail(Long crewSno, Long viewerMemberSno, String lang) {
        Crew crew = crewRepositoryPort.findBySno(crewSno)
                .orElseThrow(() -> new IllegalArgumentException("크루를 찾을 수 없습니다"));
        return toDetail(crew, viewerMemberSno, Language.fromCode(lang));
    }

    @Override
    public CrewDetailResponse getMyCrew(Long memberSno, String lang) {
        return crewMemberRepositoryPort.findByMemberSno(memberSno)
                .flatMap(cm -> crewRepositoryPort.findBySno(cm.getCrewSno()))
                .map(crew -> toDetail(crew, memberSno, Language.fromCode(lang)))
                .orElse(null);
    }

    @Override
    public List<CrewRankingEntryResponse> getRanking(Long viewerMemberSno, Long crewSno, String lang) {
        crewRepositoryPort.findBySno(crewSno)
                .orElseThrow(() -> new IllegalArgumentException("크루를 찾을 수 없습니다"));
        if (crewMemberRepositoryPort.findByCrewSnoAndMemberSno(crewSno, viewerMemberSno).isEmpty()) {
            throw new IllegalArgumentException("크루원만 볼 수 있습니다");
        }

        Language language = Language.fromCode(lang);
        List<CrewMember> members = crewMemberRepositoryPort.findByCrewSnoOrderByJoinedAtAsc(crewSno);
        List<Long> memberSnos = members.stream().map(CrewMember::getMemberSno).toList();
        Map<Long, MemberSeasonScore> scores = getMemberSeasonScoresPort.getCurrentSeasonScores(memberSnos);

        List<CrewMember> sorted = members.stream()
                .sorted(Comparator.comparingInt((CrewMember m) -> scoreOf(scores, m.getMemberSno())).reversed())
                .toList();

        return java.util.stream.IntStream.range(0, sorted.size())
                .mapToObj(i -> {
                    CrewMember m = sorted.get(i);
                    MemberSeasonScore s = scores.get(m.getMemberSno());
                    RankTier tier = s == null ? DEFAULT_TIER : s.tier();
                    return new CrewRankingEntryResponse(
                            i + 1,
                            m.getMemberSno(),
                            getMemberNicknamePort.getNickname(m.getMemberSno()),
                            s == null ? DEFAULT_SCORE : s.totalScore(),
                            tier,
                            RankTierLabelPolicy.label(tier, language),
                            m.isLeader());
                })
                .toList();
    }

    // ── GetMemberCrewBadgePort ───────────────────────────────────────────────

    @Override
    public Optional<GetMemberCrewBadgePort.CrewBadge> getBadge(Long memberSno) {
        return crewMemberRepositoryPort.findByMemberSno(memberSno)
                .flatMap(cm -> crewRepositoryPort.findBySno(cm.getCrewSno()))
                .map(c -> new GetMemberCrewBadgePort.CrewBadge(c.getName(), c.getIconUrl()));
    }

    @Override
    public Map<Long, GetMemberCrewBadgePort.CrewBadge> getBadges(Collection<Long> memberSnos) {
        return crewMemberRepositoryPort.findBadgesByMemberSnos(memberSnos);
    }

    // ── internal ────────────────────────────────────────────────────────────

    private static int scoreOf(Map<Long, MemberSeasonScore> scores, Long memberSno) {
        MemberSeasonScore s = scores.get(memberSno);
        return s == null ? DEFAULT_SCORE : s.totalScore();
    }

    private CrewDetailResponse toDetail(Crew crew, Long viewerMemberSno, Language lang) {
        boolean viewerIsMember = viewerMemberSno != null
                && crewMemberRepositoryPort.findByCrewSnoAndMemberSno(crew.getSno(), viewerMemberSno).isPresent();
        boolean viewerIsLeader = crew.isLeader(viewerMemberSno);

        String notice = null;
        List<CrewMemberResponse> members = List.of();
        if (viewerIsMember) {
            notice = crew.getNotice();
            members = crewMemberRepositoryPort.findByCrewSnoOrderByJoinedAtAsc(crew.getSno()).stream()
                    .map(cm -> {
                        RankTier tier = getMemberTierPort.getTier(cm.getMemberSno());
                        return new CrewMemberResponse(
                                cm.getMemberSno(),
                                getMemberNicknamePort.getNickname(cm.getMemberSno()),
                                cm.getRole(),
                                tier,
                                RankTierLabelPolicy.label(tier, lang),
                                cm.getJoinedAt());
                    })
                    .toList();
        }

        return new CrewDetailResponse(
                crew.getSno(), crew.getName(), crew.getIconUrl(), crew.getDescription(), notice,
                crew.getMemberCount(), crew.getMemberLimit(), crew.getJoinPolicy(), crew.getLeaderMemberSno(),
                viewerIsLeader, viewerIsMember, members);
    }
}
