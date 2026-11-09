package com.paceleague.notification.application.service;

import com.paceleague.board.domain.policy.PostContentSanitizer;
import com.paceleague.member.application.port.in.shared.GetMemberNicknamePort;
import com.paceleague.notification.application.dto.AdminNotificationDetail;
import com.paceleague.notification.application.dto.AdminNotificationSummary;
import com.paceleague.notification.application.dto.NoticeResponse;
import com.paceleague.notification.application.port.in.GetNoticeUseCase;
import com.paceleague.notification.application.port.in.shared.AdminNotificationUseCase;
import com.paceleague.notification.application.port.in.shared.SendMemberPushPort;
import com.paceleague.notification.application.port.out.AdminNotificationRepositoryPort;
import com.paceleague.notification.domain.entity.AdminNotification;
import com.paceleague.notification.domain.entity.AdminNotificationTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 관리자 알림관리 — 고른 회원들에게 개인 푸시. 본문은 게시글 에디터 HTML(이미지 포함)이라 푸시에는 평문 요약만 싣고,
// 앱은 딥링크(paceleague://notifications/{sno})로 GET /api/notification/notice/{sno} 를 불러 전체를 보여준다.
// send 는 트랜잭션을 걸지 않는다 — 회원별 FCM 호출이 길어질 수 있고(MemberPushSender 가 회원마다 저장/실패를 따로 처리),
// 알림 행을 먼저 저장해야 딥링크에 sno 를 넣을 수 있기 때문.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminNotificationService implements AdminNotificationUseCase, GetNoticeUseCase {

    static final String PUSH_KIND = "ADMIN_NOTICE";
    static final String DEEPLINK_PREFIX = "paceleague://notifications/";
    static final int TITLE_MAX_LENGTH = 200;
    static final int MAX_TARGETS = 1000;
    static final int PUSH_BODY_MAX_LENGTH = 150;
    static final int LIST_NICKNAMES = 3;
    static final String IMAGE_ONLY_BODY = "새 알림이 도착했어요.";
    private static final int PAGE_SIZE_MAX = 100;

    private final AdminNotificationRepositoryPort adminNotificationRepositoryPort;
    private final SendMemberPushPort sendMemberPushPort;
    private final GetMemberNicknamePort getMemberNicknamePort;

    @Override
    public Page<AdminNotificationSummary> list(int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), PAGE_SIZE_MAX);
        Page<AdminNotification> rows = adminNotificationRepositoryPort.findAll(
                PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "sno")));

        // 행마다 받는 회원 앞쪽 몇 명만 이름으로 — 대상/닉네임은 한 번씩만 조회한다.
        Map<Long, List<Long>> firstTargets = adminNotificationRepositoryPort
                .findTargetsIn(rows.getContent().stream().map(AdminNotification::getSno).toList()).stream()
                .collect(Collectors.groupingBy(AdminNotificationTarget::getNotificationSno, LinkedHashMap::new,
                        Collectors.mapping(AdminNotificationTarget::getMemberSno, Collectors.toList())));
        firstTargets.replaceAll((sno, members) -> members.stream().limit(LIST_NICKNAMES).toList());
        Map<Long, String> nicknames = getMemberNicknamePort.getNicknames(
                firstTargets.values().stream().flatMap(List::stream).collect(Collectors.toSet()));

        return rows.map(n -> new AdminNotificationSummary(
                n.getSno(), n.getTitle(), pushBody(n.getContent()), n.getTargetCount(), n.getSentCount(),
                firstTargets.getOrDefault(n.getSno(), List.of()).stream().map(nicknames::get).toList(),
                n.getCreatedAt()));
    }

    @Override
    public AdminNotificationDetail get(Long notificationSno) {
        return toDetail(load(notificationSno));
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AdminNotificationDetail send(Long adminSno, String title, String content, List<Long> memberSnos) {
        String safeTitle = validateTitle(title);
        String sanitized = sanitizeContent(content);
        List<Long> targets = validateTargets(memberSnos);

        AdminNotification notification = adminNotificationRepositoryPort.save(
                AdminNotification.create(safeTitle, sanitized, targets.size(), adminSno));

        String deeplink = DEEPLINK_PREFIX + notification.getSno();
        Map<String, String> data = new LinkedHashMap<>();
        data.put("type", "notice");
        data.put("notificationSno", String.valueOf(notification.getSno()));
        data.put("deeplink", deeplink);
        String body = pushBody(sanitized);

        List<AdminNotificationTarget> results = new ArrayList<>();
        int sent = 0;
        for (Long memberSno : targets) {
            boolean ok = sendMemberPushPort.send(memberSno, PUSH_KIND, safeTitle, body, deeplink, data);
            if (ok) sent++;
            results.add(AdminNotificationTarget.of(notification.getSno(), memberSno, ok));
        }
        adminNotificationRepositoryPort.saveTargets(results);
        notification.recordSent(sent);
        return toDetail(adminNotificationRepositoryPort.save(notification), results);
    }

    @Override
    public NoticeResponse getNotice(Long memberSno, Long notificationSno) {
        AdminNotification notification = load(notificationSno);
        if (!adminNotificationRepositoryPort.isTarget(notificationSno, memberSno)) {
            throw new IllegalArgumentException("Notification not found.");
        }
        return NoticeResponse.from(notification);
    }

    private AdminNotification load(Long notificationSno) {
        return adminNotificationRepositoryPort.findById(notificationSno)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found."));
    }

    private AdminNotificationDetail toDetail(AdminNotification n) {
        return toDetail(n, adminNotificationRepositoryPort.findTargets(n.getSno()));
    }

    private AdminNotificationDetail toDetail(AdminNotification n, List<AdminNotificationTarget> targets) {
        Map<Long, String> nicknames = getMemberNicknamePort.getNicknames(
                targets.stream().map(AdminNotificationTarget::getMemberSno).toList());
        List<AdminNotificationDetail.Target> views = targets.stream()
                .map(t -> new AdminNotificationDetail.Target(t.getMemberSno(), nicknames.get(t.getMemberSno()), t.isSent()))
                .toList();
        return new AdminNotificationDetail(n.getSno(), n.getTitle(), n.getContent(), n.getTargetCount(),
                n.getSentCount(), views, n.getCreatedAt());
    }

    private String validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required.");
        }
        String trimmed = title.trim();
        if (trimmed.length() > TITLE_MAX_LENGTH) {
            throw new IllegalArgumentException("Title must be 200 characters or fewer.");
        }
        return trimmed;
    }

    // 게시글/팝업과 같은 에디터·같은 허용 태그 — board 의 정제기를 그대로 쓴다(PopupService 와 같은 예외).
    private String sanitizeContent(String content) {
        String sanitized = PostContentSanitizer.sanitize(content);
        if (sanitized == null
                || (PostContentSanitizer.toPlainText(sanitized).isBlank() && !PostContentSanitizer.containsMedia(sanitized))) {
            throw new IllegalArgumentException("Content is required.");
        }
        return sanitized;
    }

    private List<Long> validateTargets(List<Long> memberSnos) {
        if (memberSnos == null) {
            throw new IllegalArgumentException("Select at least one member.");
        }
        List<Long> distinct = new ArrayList<>(new LinkedHashSet<>(memberSnos));
        distinct.removeIf(sno -> sno == null);
        if (distinct.isEmpty()) {
            throw new IllegalArgumentException("Select at least one member.");
        }
        if (distinct.size() > MAX_TARGETS) {
            throw new IllegalArgumentException("You can select up to 1000 members.");
        }
        return distinct;
    }

    // 푸시 본문 = 본문 HTML 의 평문 앞부분. 이미지만 있는 알림은 고정 문구.
    static String pushBody(String sanitizedContent) {
        String text = PostContentSanitizer.toPlainText(sanitizedContent);
        if (text.isEmpty()) {
            return IMAGE_ONLY_BODY;
        }
        return text.length() <= PUSH_BODY_MAX_LENGTH ? text : text.substring(0, PUSH_BODY_MAX_LENGTH) + "…";
    }
}
