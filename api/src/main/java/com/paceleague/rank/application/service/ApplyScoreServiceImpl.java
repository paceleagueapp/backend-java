package com.paceleague.rank.application.service;

import com.paceleague.rank.application.dto.ApplyScoreCommand;
import com.paceleague.rank.application.port.in.ApplyScoreUseCase;
import com.paceleague.rank.application.port.out.MemberScoreRepositoryPort;
import com.paceleague.rank.application.port.out.RankRepositoryPort;
import com.paceleague.rank.domain.entity.MemberScore;
import com.paceleague.rank.domain.entity.Rank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// record 도메인의 RecordServiceImpl.saveRank/applyScoreToSeason에 있던 로직을 rank 도메인 소유로 이전한 유스케이스.
@Service
@RequiredArgsConstructor
@Transactional
public class ApplyScoreServiceImpl implements ApplyScoreUseCase {

    private final RankRepositoryPort rankRepositoryPort;
    private final MemberScoreRepositoryPort memberScoreRepositoryPort;

    public void applyScore(ApplyScoreCommand command) {
        rankRepositoryPort.save(Rank.create(
                command.memberSno(),
                command.totalScore(),
                command.scaledScore(),
                command.addScore(),
                command.utcOffset()
        ));

        MemberScore memberScore = memberScoreRepositoryPort
                .findByMemberSnoAndSeasonSnoForUpdate(command.memberSno(), command.seasonSno())
                .orElseGet(() -> MemberScore.builder()
                        .memberSno(command.memberSno())
                        .seasonSno(command.seasonSno())
                        .totalScore(1500)
                        .build());

        memberScore.addScore(command.totalScore());
        memberScoreRepositoryPort.save(memberScore);
    }
}
