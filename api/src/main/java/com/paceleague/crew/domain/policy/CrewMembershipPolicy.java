package com.paceleague.crew.domain.policy;

import com.paceleague.crew.domain.entity.Crew;

// 크루 가입/탈퇴/권한 판정의 순수 규칙. 초대 수락·가입신청 승인 양쪽에서 재사용.
public final class CrewMembershipPolicy {

    private CrewMembershipPolicy() {
    }

    // 대상 크루에 새 회원이 들어갈 수 있는지. (크루 락을 잡은 뒤 최종 확정 직전에 호출)
    public static void assertJoinable(Crew crew, boolean applicantAlreadyInACrew) {
        if (crew == null || !crew.isActive()) {
            throw new IllegalArgumentException("크루를 찾을 수 없습니다");
        }
        if (applicantAlreadyInACrew) {
            throw new IllegalArgumentException("이미 다른 크루에 소속되어 있습니다");
        }
        if (crew.isFull()) {
            throw new IllegalArgumentException("크루 정원이 가득 찼습니다");
        }
    }

    public static void assertLeader(Crew crew, Long memberSno) {
        if (crew == null || !crew.isActive()) {
            throw new IllegalArgumentException("크루를 찾을 수 없습니다");
        }
        if (!crew.isLeader(memberSno)) {
            throw new IllegalArgumentException("크루장만 할 수 있는 작업입니다");
        }
    }

    // 크루장이 크루를 떠나려면 먼저 위임하거나 해체해야 한다.
    public static void assertLeaderCanLeave(Crew crew, Long memberSno) {
        if (crew.isLeader(memberSno)) {
            throw new IllegalArgumentException("크루장은 크루장 위임 또는 크루 해체 후에만 탈퇴할 수 있습니다");
        }
    }

    // 해체는 크루장 혼자만 남았을 때만(다른 크루원이 남아 있으면 먼저 내보내거나 위임).
    public static void assertDisbandable(Crew crew) {
        if (crew.getMemberCount() > 1) {
            throw new IllegalArgumentException("크루원이 남아 있어 해체할 수 없습니다. 크루원을 모두 내보낸 뒤 해체하세요");
        }
    }
}
