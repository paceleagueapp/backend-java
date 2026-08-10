package com.example.paceleague.member.application.service;

import com.example.paceleague.member.application.port.in.GetMemberNicknamePort;
import com.example.paceleague.member.application.port.out.MemberRepositoryPort;
import com.example.paceleague.member.domain.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryServiceImpl implements GetMemberNicknamePort {

    private final MemberRepositoryPort memberRepositoryPort;

    public String getNickname(Long memberSno) {
        return memberRepositoryPort.findBySno(memberSno)
                .map(Member::getNickname)
                .orElse("알 수 없음");
    }
}
