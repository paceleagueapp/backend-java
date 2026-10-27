package com.paceleague.popup.application.port.in.shared;

import com.paceleague.popup.application.dto.AdminPopupResponse;
import com.paceleague.popup.application.dto.PopupCommand;
import org.springframework.data.domain.Page;

// 관리자 팝업관리 화면(admin 도메인)이 호출하는 포트.
public interface AdminPopupUseCase {
    Page<AdminPopupResponse> list(int page, int size);

    AdminPopupResponse get(Long popupSno);

    AdminPopupResponse create(PopupCommand command);

    AdminPopupResponse update(Long popupSno, PopupCommand command);

    AdminPopupResponse changeActive(Long popupSno, boolean active);

    void delete(Long popupSno);
}
