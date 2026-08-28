package com.example.paceleague.crew.domain.policy;

import com.example.paceleague.crew.domain.entity.Crew;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrewMembershipPolicyTest {

    private Crew crew(int memberLimit, int memberCount, long leaderSno) {
        Crew c = Crew.create("테스트크루", null, null, memberLimit, leaderSno);
        for (int i = 1; i < memberCount; i++) {
            c.increaseMemberCount();
        }
        return c;
    }

    @Test
    void 정원_여유가_있고_소속이_없으면_가입_가능() {
        assertThatCode(() -> CrewMembershipPolicy.assertJoinable(crew(30, 5, 1L), false))
                .doesNotThrowAnyException();
    }

    @Test
    void 이미_다른_크루_소속이면_거부() {
        assertThatThrownBy(() -> CrewMembershipPolicy.assertJoinable(crew(30, 5, 1L), true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 정원이_가득_차면_거부() {
        assertThatThrownBy(() -> CrewMembershipPolicy.assertJoinable(crew(5, 5, 1L), false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 크루장만_할_수_있는_작업_검증() {
        Crew c = crew(30, 3, 7L);
        assertThatCode(() -> CrewMembershipPolicy.assertLeader(c, 7L)).doesNotThrowAnyException();
        assertThatThrownBy(() -> CrewMembershipPolicy.assertLeader(c, 8L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 크루장은_바로_탈퇴할_수_없다() {
        Crew c = crew(30, 3, 7L);
        assertThatThrownBy(() -> CrewMembershipPolicy.assertLeaderCanLeave(c, 7L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatCode(() -> CrewMembershipPolicy.assertLeaderCanLeave(c, 8L)).doesNotThrowAnyException();
    }

    @Test
    void 크루원이_남아있으면_해체_불가() {
        assertThatThrownBy(() -> CrewMembershipPolicy.assertDisbandable(crew(30, 2, 7L)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatCode(() -> CrewMembershipPolicy.assertDisbandable(crew(30, 1, 7L)))
                .doesNotThrowAnyException();
    }
}
