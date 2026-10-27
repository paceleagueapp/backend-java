package com.paceleague.popup.adapter.in.web;

import com.paceleague.common.response.ResponseApi;
import com.paceleague.popup.application.dto.PopupResponse;
import com.paceleague.popup.application.port.in.GetActivePopupsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/popup")
@RequiredArgsConstructor
@Tag(name = "Popup", description = "공지 팝업 — 관리자가 등록한 팝업 중 지금 노출할 것. 공개(로그인 불필요).")
public class PopupController {

    private final GetActivePopupsUseCase getActivePopupsUseCase;

    @Operation(summary = "노출 중인 팝업 목록",
            description = "활성 상태이면서 지금(KST)이 노출 기간(startAt 이상, endAt 미만) 안인 팝업만, 시작일 최신순. "
                    + "요청값 없음. 웹/앱 구분 없이 내려주므로 target(WEB/APP/BOTH)으로 걸러서 쓴다 "
                    + "(웹은 WEB·BOTH, 앱은 APP·BOTH). content 는 정제된 HTML.")
    @ApiResponse(responseCode = "200", description = "조회 성공(없으면 빈 배열)")
    @GetMapping
    public ResponseApi<List<PopupResponse>> getActivePopups() {
        return ResponseApi.success(getActivePopupsUseCase.getActivePopups());
    }
}
