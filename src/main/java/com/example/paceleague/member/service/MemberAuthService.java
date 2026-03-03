package com.example.paceleague.member.service;

import com.example.paceleague.common.config.JwtProperties;
import com.example.paceleague.member.dto.TokenResponse;
import com.example.paceleague.member.entity.Member;
import com.example.paceleague.member.repository.MemberRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberAuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties props;

    public MemberAuthService(MemberRepository memberRepository,
                             PasswordEncoder passwordEncoder,
                             RefreshTokenService refreshTokenService,
                             JwtProperties props) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.props = props;
        this.jwtTokenProvider = new JwtTokenProvider(props);
    }

    @Transactional
    public void join(String memberId, String rawPassword, String name, String email) {

        if (memberRepository.existsByMemberId(memberId)) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        String hash = passwordEncoder.encode(rawPassword);
        Member member = new Member(memberId, hash, name, email);

        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(String memberId, String rawPassword) {

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(rawPassword, member.getPasswordHash())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        String access = jwtTokenProvider.createAccessToken(member.getSno(), member.getMemberId());
        String refresh = refreshTokenService.issue(member.getSno());

        return new TokenResponse(
                "Bearer",
                access,
                props.accessTokenTtlSeconds(),
                refresh,
                props.refreshTokenTtlSeconds()
        );
    }
}
