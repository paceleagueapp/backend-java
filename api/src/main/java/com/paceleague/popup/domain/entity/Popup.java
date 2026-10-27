package com.paceleague.popup.domain.entity;

import com.paceleague.popup.domain.enums.PopupTarget;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 관리자 팝업. start_at/end_at 은 관리자가 화면에서 입력한 한국시간(KST) 벽시계 값 그대로 저장하고,
// 노출 여부도 KST 현재시각과 비교한다(PopupService) — 서버 JVM 이 UTC 라도 관리자가 본 시각과 어긋나지 않게.
@Entity
@Table(name = "popup")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Popup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sno;

    @Column(nullable = false, length = 200)
    private String title;

    // 게시글과 같은 에디터로 쓴 HTML — 저장 전에 PostContentSanitizer 로 정제된 값만 들어온다.
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PopupTarget target;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @Column(name = "update_at", nullable = false)
    private LocalDateTime updateAt;

    private Popup(String title, String content, PopupTarget target, LocalDateTime startAt, LocalDateTime endAt, boolean active) {
        this.title = title;
        this.content = content;
        this.target = target;
        this.startAt = startAt;
        this.endAt = endAt;
        this.active = active;
        this.createAt = LocalDateTime.now();
        this.updateAt = this.createAt;
    }

    public static Popup create(String title, String content, PopupTarget target,
                               LocalDateTime startAt, LocalDateTime endAt, boolean active) {
        return new Popup(title, content, target, startAt, endAt, active);
    }

    public void update(String title, String content, PopupTarget target,
                       LocalDateTime startAt, LocalDateTime endAt, boolean active) {
        this.title = title;
        this.content = content;
        this.target = target;
        this.startAt = startAt;
        this.endAt = endAt;
        this.active = active;
        this.updateAt = LocalDateTime.now();
    }

    public void changeActive(boolean active) {
        this.active = active;
        this.updateAt = LocalDateTime.now();
    }

    // 활성 + 기간 안(start <= now < end) 이면 지금 노출 중.
    public boolean isVisibleAt(LocalDateTime now) {
        return active && !now.isBefore(startAt) && now.isBefore(endAt);
    }
}
