package com.paceleague.popup.application.port.in;

import com.paceleague.popup.application.dto.PopupResponse;

import java.util.List;

public interface GetActivePopupsUseCase {
    // 활성 + 지금(KST)이 기간 안인 팝업 전부, 시작일 최신순. 웹/앱 구분 없이 내려주고 target 으로 클라이언트가 거른다.
    List<PopupResponse> getActivePopups();
}
