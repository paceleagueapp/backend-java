package com.paceleague.member.application.service;

import com.paceleague.board.application.port.in.shared.PurgeMemberBoardPort;
import com.paceleague.crew.application.port.in.shared.LeaveCrewOnWithdrawPort;
import com.paceleague.media.application.port.in.shared.PurgeMemberMediaPort;
import com.paceleague.member.application.port.in.MemberWithdrawUseCase;
import com.paceleague.member.application.port.out.MemberRepositoryPort;
import com.paceleague.member.domain.entity.Member;
import com.paceleague.rank.application.port.in.shared.PurgeMemberRankPort;
import com.paceleague.record.application.port.in.shared.PurgeMemberRecordsPort;
import com.paceleague.territory.application.port.in.shared.PurgeMemberTerritoryPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberWithdrawService implements MemberWithdrawUseCase {

    private static final Logger log = LoggerFactory.getLogger(MemberWithdrawService.class);

    private final MemberRepositoryPort memberRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final LeaveCrewOnWithdrawPort leaveCrewOnWithdrawPort;
    private final PurgeMemberRecordsPort purgeMemberRecordsPort;
    private final PurgeMemberRankPort purgeMemberRankPort;
    private final PurgeMemberTerritoryPort purgeMemberTerritoryPort;
    private final PurgeMemberMediaPort purgeMemberMediaPort;
    private final PurgeMemberBoardPort purgeMemberBoardPort;

    @Override
    @Transactional
    public void withdraw(Long memberSno, String rawPassword) {
        Member member = memberRepositoryPort.findBySno(memberSno)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        if (!member.isActive()) {
            return; // 이미 탈퇴 처리됨 — 멱등
        }
        if (rawPassword == null || !passwordEncoder.matches(rawPassword, member.getPasswordHash())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        // 크루장이면 여기서 400 — member/데이터는 아직 그대로.
        leaveCrewOnWithdrawPort.onMemberWithdraw(memberSno);

        purgeMemberRecordsPort.purge(memberSno);
        purgeMemberRankPort.purge(memberSno);
        purgeMemberTerritoryPort.purge(memberSno);
        purgeMemberMediaPort.purge(memberSno);
        purgeMemberBoardPort.purge(memberSno);

        member.withdraw();
        memberRepositoryPort.save(member);

        log.info("회원 탈퇴 완료: memberSno={}", memberSno);
    }
}
