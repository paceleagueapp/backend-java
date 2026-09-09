package com.paceleague.member.adapter.out.persistence;

import com.paceleague.member.application.port.out.MemberBlockRepositoryPort;
import com.paceleague.member.domain.entity.MemberBlock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class MemberBlockPersistenceAdapter implements MemberBlockRepositoryPort {

    private final MemberBlockJpaRepository jpa;

    public MemberBlock save(MemberBlock block) {
        return jpa.save(block);
    }

    public boolean exists(Long blockerMemberSno, Long blockedMemberSno) {
        return jpa.existsByBlockerMemberSnoAndBlockedMemberSno(blockerMemberSno, blockedMemberSno);
    }

    @Transactional
    public void delete(Long blockerMemberSno, Long blockedMemberSno) {
        jpa.deleteByBlockerMemberSnoAndBlockedMemberSno(blockerMemberSno, blockedMemberSno);
    }

    public List<MemberBlock> findByBlocker(Long blockerMemberSno) {
        return jpa.findByBlockerMemberSnoOrderByCreatedAtDesc(blockerMemberSno);
    }

    public Set<Long> findBlockedSnos(Long blockerMemberSno) {
        return new HashSet<>(jpa.findBlockedSnos(blockerMemberSno));
    }

    @Transactional
    public void deleteAllByMember(Long memberSno) {
        jpa.deleteAllByMember(memberSno);
    }
}
