package com.paceleague.member.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 동의 유형별 최신 상태 1행만 유지(UNIQUE member_sno+agreement_type). agreed=false 행도 남겨
// "언제 무엇을 거부/철회했는지"까지 확인자료로 남긴다(위치정보법 제16조).
@Entity
@Table(name = "member_agreement")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sno;

    @Column(name = "member_sno", nullable = false)
    private Long memberSno;

    @Enumerated(EnumType.STRING)
    @Column(name = "agreement_type", nullable = false, length = 30)
    private AgreementType agreementType;

    @Column(nullable = false)
    private boolean agreed;

    @Column(name = "agreement_version", nullable = false, length = 20)
    private String agreementVersion;

    @Column(name = "agreed_at", nullable = false)
    private LocalDateTime agreedAt;

    private MemberAgreement(Long memberSno, AgreementType agreementType, boolean agreed) {
        this.memberSno = memberSno;
        this.agreementType = agreementType;
        this.agreed = agreed;
        this.agreementVersion = agreementType.currentVersion();
        this.agreedAt = LocalDateTime.now();
    }

    public static MemberAgreement create(Long memberSno, AgreementType agreementType, boolean agreed) {
        return new MemberAgreement(memberSno, agreementType, agreed);
    }

    public void update(boolean agreed) {
        this.agreed = agreed;
        this.agreementVersion = this.agreementType.currentVersion();
        this.agreedAt = LocalDateTime.now();
    }
}
