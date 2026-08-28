package com.example.paceleague.rank.adapter.out.persistence;

import com.example.paceleague.rank.application.port.out.MemberScoreRepositoryPort;
import com.example.paceleague.rank.domain.entity.MemberScore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberScorePersistenceAdapter implements MemberScoreRepositoryPort {

    private final MemberScoreJpaRepository memberScoreJpaRepository;

    public Optional<MemberScore> findByMemberSnoAndSeasonSnoForUpdate(Long memberSno, Long seasonSno) {
        return memberScoreJpaRepository.findByMemberSnoAndSeasonSnoForUpdate(memberSno, seasonSno);
    }

    public Optional<MemberScore> findByMemberSnoAndSeasonSno(Long memberSno, Long seasonSno) {
        return memberScoreJpaRepository.findByMemberSnoAndSeasonSno(memberSno, seasonSno);
    }

    public List<MemberScore> findByMemberSnosAndSeasonSno(Collection<Long> memberSnos, Long seasonSno) {
        if (memberSnos == null || memberSnos.isEmpty()) {
            return List.of();
        }
        return memberScoreJpaRepository.findByMemberSnoInAndSeasonSno(memberSnos, seasonSno);
    }

    public MemberScore save(MemberScore memberScore) {
        return memberScoreJpaRepository.save(memberScore);
    }
}
