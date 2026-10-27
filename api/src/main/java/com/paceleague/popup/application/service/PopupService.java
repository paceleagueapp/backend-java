package com.paceleague.popup.application.service;

import com.paceleague.board.domain.policy.PostContentSanitizer;
import com.paceleague.popup.application.dto.AdminPopupResponse;
import com.paceleague.popup.application.dto.PopupCommand;
import com.paceleague.popup.application.dto.PopupResponse;
import com.paceleague.popup.application.port.in.GetActivePopupsUseCase;
import com.paceleague.popup.application.port.in.shared.AdminPopupUseCase;
import com.paceleague.popup.application.port.out.PopupRepositoryPort;
import com.paceleague.popup.domain.entity.Popup;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopupService implements GetActivePopupsUseCase, AdminPopupUseCase {

    // 관리자가 입력하는 기간이 한국시간이라 비교 기준도 KST 현재시각.
    static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int TITLE_MAX_LENGTH = 200;
    private static final int PAGE_SIZE_MAX = 100;

    private final PopupRepositoryPort popupRepositoryPort;

    @Override
    public List<PopupResponse> getActivePopups() {
        return popupRepositoryPort.findVisibleAt(nowKst()).stream()
                .map(PopupResponse::from)
                .toList();
    }

    @Override
    public Page<AdminPopupResponse> list(int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), PAGE_SIZE_MAX);
        LocalDateTime now = nowKst();
        return popupRepositoryPort.findAll(PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "sno")))
                .map(p -> AdminPopupResponse.from(p, now));
    }

    @Override
    public AdminPopupResponse get(Long popupSno) {
        return AdminPopupResponse.from(load(popupSno), nowKst());
    }

    @Override
    @Transactional
    public AdminPopupResponse create(PopupCommand command) {
        String content = validateAndSanitize(command);
        Popup popup = Popup.create(command.title().trim(), content, command.target(),
                command.startAt(), command.endAt(), activeOrDefault(command));
        return AdminPopupResponse.from(popupRepositoryPort.save(popup), nowKst());
    }

    @Override
    @Transactional
    public AdminPopupResponse update(Long popupSno, PopupCommand command) {
        Popup popup = load(popupSno);
        String content = validateAndSanitize(command);
        popup.update(command.title().trim(), content, command.target(),
                command.startAt(), command.endAt(), activeOrDefault(command));
        return AdminPopupResponse.from(popupRepositoryPort.save(popup), nowKst());
    }

    @Override
    @Transactional
    public AdminPopupResponse changeActive(Long popupSno, boolean active) {
        Popup popup = load(popupSno);
        popup.changeActive(active);
        return AdminPopupResponse.from(popupRepositoryPort.save(popup), nowKst());
    }

    @Override
    @Transactional
    public void delete(Long popupSno) {
        popupRepositoryPort.delete(load(popupSno));
    }

    private Popup load(Long popupSno) {
        return popupRepositoryPort.findById(popupSno)
                .orElseThrow(() -> new IllegalArgumentException("Popup not found."));
    }

    // 게시글과 같은 에디터/같은 허용 태그 — board 의 정제기를 그대로 쓴다(상태 없는 static 정책이라
    // RankTierLabelPolicy 와 같은 "순수 정책 직접 호출" 예외).
    private String validateAndSanitize(PopupCommand command) {
        if (command.title() == null || command.title().isBlank()) {
            throw new IllegalArgumentException("Title is required.");
        }
        if (command.title().trim().length() > TITLE_MAX_LENGTH) {
            throw new IllegalArgumentException("Title must be 200 characters or fewer.");
        }
        if (command.target() == null) {
            throw new IllegalArgumentException("Target is required.");
        }
        if (command.startAt() == null || command.endAt() == null) {
            throw new IllegalArgumentException("Start and end time are required.");
        }
        if (!command.endAt().isAfter(command.startAt())) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
        String sanitized = PostContentSanitizer.sanitize(command.content());
        if (sanitized == null
                || (PostContentSanitizer.toPlainText(sanitized).isBlank() && !PostContentSanitizer.containsMedia(sanitized))) {
            throw new IllegalArgumentException("Content is required.");
        }
        return sanitized;
    }

    private boolean activeOrDefault(PopupCommand command) {
        return command.active() == null || command.active();
    }

    LocalDateTime nowKst() {
        return LocalDateTime.now(KST);
    }
}
