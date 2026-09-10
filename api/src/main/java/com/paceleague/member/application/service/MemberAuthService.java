package com.paceleague.member.application.service;

import com.paceleague.common.config.JwtProperties;
import com.paceleague.member.application.dto.AuthTokenInfo;
import com.paceleague.member.application.port.in.MemberAuthUseCase;
import com.paceleague.member.application.port.out.MemberAgreementRepositoryPort;
import com.paceleague.member.application.port.out.MemberRepositoryPort;
import com.paceleague.member.application.port.out.RefreshTokenStorePort;
import com.paceleague.member.application.port.out.TokenIssuerPort;
import com.paceleague.member.domain.entity.AgreementType;
import com.paceleague.member.domain.entity.Member;
import com.paceleague.member.domain.entity.MemberAgreement;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@Transactional(readOnly = true)
public class MemberAuthService implements MemberAuthUseCase {
    // 계정당 15분 내 5회 로그인 실패 시 잠금 — 짧은 비밀번호에 대한 무차별 대입 공격 방어.
    private static final int LOGIN_FAIL_LIMIT = 5;
    private static final Duration LOGIN_LOCKOUT_WINDOW = Duration.ofMinutes(15);

    private final MemberRepositoryPort memberRepositoryPort;
    private final MemberAgreementRepositoryPort memberAgreementRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenStorePort refreshTokenStorePort;
    private final TokenIssuerPort tokenIssuerPort;
    private final JwtProperties props;
    private final StringRedisTemplate redis;

    public MemberAuthService(MemberRepositoryPort memberRepositoryPort,
                                 MemberAgreementRepositoryPort memberAgreementRepositoryPort,
                                 PasswordEncoder passwordEncoder,
                                 RefreshTokenStorePort refreshTokenStorePort,
                                 TokenIssuerPort tokenIssuerPort,
                                 JwtProperties props,
                                 StringRedisTemplate redis) {
        this.memberRepositoryPort = memberRepositoryPort;
        this.memberAgreementRepositoryPort = memberAgreementRepositoryPort;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenStorePort = refreshTokenStorePort;
        this.props = props;
        this.tokenIssuerPort = tokenIssuerPort;
        this.redis = redis;
    }

    @Transactional
    public AuthTokenInfo join(String memberId, String rawPassword, String nickname, String email,
                               boolean agreedTerms, boolean agreedPrivacy, boolean agreedLocation) {

        // 이용약관/개인정보처리방침/위치정보 수집·이용 — 위치정보는 GPS 기반 핵심 기능(러닝 기록/땅따먹기) 없이는
        // 서비스 자체가 성립하지 않는 앱 특성상 셋 다 필수 동의로 처리(가입 자체를 막음).
        if (!agreedTerms || !agreedPrivacy || !agreedLocation) {
            throw new IllegalArgumentException("이용약관, 개인정보 처리방침, 위치정보 수집·이용에 모두 동의해야 회원가입할 수 있습니다.");
        }

        if (memberRepositoryPort.existsByMemberId(memberId)) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        String hash = passwordEncoder.encode(rawPassword);
        Member member = Member.create(memberId, hash, nickname, email);

        memberRepositoryPort.save(member);

        Long memberSno = member.getSno().longValue();
        memberAgreementRepositoryPort.save(MemberAgreement.create(memberSno, AgreementType.TERMS, true));
        memberAgreementRepositoryPort.save(MemberAgreement.create(memberSno, AgreementType.PRIVACY, true));
        memberAgreementRepositoryPort.save(MemberAgreement.create(memberSno, AgreementType.LOCATION, true));

        return issueTokens(member);
    }

    @Transactional(readOnly = true)
    public AuthTokenInfo login(String memberId, String rawPassword) {
        String failKey = "login:fail:" + memberId;

        String failCount = redis.opsForValue().get(failKey);
        if (failCount != null && Long.parseLong(failCount) >= LOGIN_FAIL_LIMIT) {
            throw new IllegalArgumentException("로그인 시도가 너무 많습니다. 잠시 후 다시 시도해주세요.");
        }

        Member member = memberRepositoryPort.findByMemberId(memberId).orElse(null);
        if (member == null || !passwordEncoder.matches(rawPassword, member.getPasswordHash())) {
            recordLoginFailure(failKey);
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        if (!member.isActive()) {
            throw new IllegalArgumentException("탈퇴 처리된 계정입니다.");
        }

        redis.delete(failKey);
        return issueTokens(member);
    }

    private void recordLoginFailure(String failKey) {
        Long count = redis.opsForValue().increment(failKey);
        if (count != null && count == 1L) {
            redis.expire(failKey, LOGIN_LOCKOUT_WINDOW);
        }
    }

    @Transactional
    public AuthTokenInfo reissue(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("refresh token is required");
        }

        Long memberSno = refreshTokenStorePort.validateAndRevoke(refreshToken);

        Member member = memberRepositoryPort.findBySno(memberSno)
                .orElseThrow(() -> new IllegalArgumentException("member not found"));

        if (!member.isActive()) {
            throw new IllegalArgumentException("탈퇴 처리된 계정입니다.");
        }

        return issueTokens(member);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("refresh token is required");
        }

        refreshTokenStorePort.revoke(refreshToken);
    }

    private AuthTokenInfo issueTokens(Member member) {
        String access = tokenIssuerPort.createAccessToken(member.getSno(), member.getMemberId());
        String refresh = refreshTokenStorePort.issue(member.getSno());

        return new AuthTokenInfo(
                "Bearer",
                access,
                props.accessTokenTtlSeconds(),
                refresh,
                props.refreshTokenTtlSeconds(),
                member.getNickname()
        );
    }
}
