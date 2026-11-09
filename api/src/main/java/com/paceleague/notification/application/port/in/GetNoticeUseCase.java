package com.paceleague.notification.application.port.in;

import com.paceleague.notification.application.dto.NoticeResponse;

// 앱이 관리자 알림 푸시를 눌렀을 때 본문(이미지 포함 HTML)을 불러온다. 받는 회원으로 지정된 회원만 조회 가능.
public interface GetNoticeUseCase {
    NoticeResponse getNotice(Long memberSno, Long notificationSno);
}
