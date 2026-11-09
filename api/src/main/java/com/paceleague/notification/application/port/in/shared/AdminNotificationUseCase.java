package com.paceleague.notification.application.port.in.shared;

import com.paceleague.notification.application.dto.AdminNotificationDetail;
import com.paceleague.notification.application.dto.AdminNotificationSummary;
import org.springframework.data.domain.Page;

import java.util.List;

// 관리자 알림관리 화면(admin 도메인)이 호출하는 포트.
public interface AdminNotificationUseCase {
    Page<AdminNotificationSummary> list(int page, int size);

    AdminNotificationDetail get(Long notificationSno);

    // 저장 후 고른 회원들에게 바로 개인 푸시를 보낸다. 결과(발송 수 포함)를 상세 형태로 돌려준다.
    AdminNotificationDetail send(Long adminSno, String title, String content, List<Long> memberSnos);
}
