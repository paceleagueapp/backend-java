package com.paceleague.notification.application.port.in.shared;

import com.paceleague.notification.application.dto.AdminDeviceTokenView;

import java.util.Collection;
import java.util.List;
import java.util.Set;

// 관리자 회원관리 화면이 회원의 FCM 기기 토큰 등록 여부를 확인하기 위한 크로스 도메인 포트.
public interface AdminDeviceTokenQueryPort {

    // 주어진 회원 중 토큰이 1개라도 등록된 회원 sno.
    Set<Long> findMemberSnosWithToken(Collection<Long> memberSnos);

    // 한 회원의 토큰 전부, 최근 사용순.
    List<AdminDeviceTokenView> findByMemberSno(Long memberSno);
}
