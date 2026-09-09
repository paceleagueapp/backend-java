package com.paceleague.member.application.service;

import com.paceleague.member.application.dto.BlockedMemberResponse;
import com.paceleague.member.application.port.in.MemberBlockUseCase;
import com.paceleague.member.application.port.in.shared.GetBlockedMemberSnosPort;
import com.paceleague.member.application.port.in.shared.GetMemberNicknamePort;
import com.paceleague.member.application.port.out.MemberBlockRepositoryPort;
import com.paceleague.member.domain.entity.MemberBlock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberBlockService implements MemberBlockUseCase, GetBlockedMemberSnosPort {

    private final MemberBlockRepositoryPort memberBlockRepositoryPort;
    private final GetMemberNicknamePort getMemberNicknamePort;

    @Override
    @Transactional
    public void block(Long blockerMemberSno, Long blockedMemberSno) {
        if (blockedMemberSno == null || blockedMemberSno.equals(blockerMemberSno)) {
            throw new IllegalArgumentException("자기 자신은 차단할 수 없습니다.");
        }
        if (!memberBlockRepositoryPort.exists(blockerMemberSno, blockedMemberSno)) {
            memberBlockRepositoryPort.save(MemberBlock.create(blockerMemberSno, blockedMemberSno));
        }
    }

    @Override
    @Transactional
    public void unblock(Long blockerMemberSno, Long blockedMemberSno) {
        memberBlockRepositoryPort.delete(blockerMemberSno, blockedMemberSno);
    }

    @Override
    public List<BlockedMemberResponse> list(Long blockerMemberSno) {
        return memberBlockRepositoryPort.findByBlocker(blockerMemberSno).stream()
                .map(b -> new BlockedMemberResponse(b.getBlockedMemberSno(),
                        getMemberNicknamePort.getNickname(b.getBlockedMemberSno())))
                .toList();
    }

    @Override
    public Set<Long> getBlockedBy(Long viewerMemberSno) {
        if (viewerMemberSno == null) {
            return Set.of();
        }
        return memberBlockRepositoryPort.findBlockedSnos(viewerMemberSno);
    }
}
