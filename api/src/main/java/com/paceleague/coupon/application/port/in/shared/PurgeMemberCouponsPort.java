package com.paceleague.coupon.application.port.in.shared;

// 회원 탈퇴 시 그 회원의 쿠폰(행 + S3 이미지)을 삭제한다.
public interface PurgeMemberCouponsPort {
    void purge(Long memberSno);
}
