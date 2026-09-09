package com.paceleague.member.application.service;

import com.paceleague.member.application.port.out.MemberRepositoryPort;
import com.paceleague.member.domain.entity.Member;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MemberQueryServiceTest {

    @Mock MemberRepositoryPort memberRepositoryPort;
    @InjectMocks MemberQueryService service;

    private Member member(int sno, String nick, boolean withdrawn) {
        Member m = Member.create("id" + sno, "H", nick, null);
        ReflectionTestUtils.setField(m, "sno", sno);
        if (withdrawn) m.withdraw();
        return m;
    }

    @Test
    void 활성_회원은_닉네임_그대로_반환한다() {
        when(memberRepositoryPort.findBySno(1L)).thenReturn(Optional.of(member(1, "달리는곰", false)));
        assertThat(service.getNickname(1L)).isEqualTo("달리는곰");
    }

    @Test
    void 탈퇴_회원은_탈퇴한_사용자로_표시한다() {
        when(memberRepositoryPort.findBySno(2L)).thenReturn(Optional.of(member(2, "곰", true)));
        assertThat(service.getNickname(2L)).isEqualTo("탈퇴한 사용자");
    }

    @Test
    void 없는_회원은_알_수_없음() {
        when(memberRepositoryPort.findBySno(9L)).thenReturn(Optional.empty());
        assertThat(service.getNickname(9L)).isEqualTo("알 수 없음");
    }

    @Test
    void 회원_검색은_탈퇴_회원을_제외한다() {
        when(memberRepositoryPort.searchByMemberIdOrNickname(anyString(), anyInt()))
                .thenReturn(List.of(member(1, "활성곰", false), member(2, "탈퇴곰", true)));

        var results = service.search("곰", 20);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).nickname()).isEqualTo("활성곰");
    }
}
