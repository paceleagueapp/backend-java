package com.paceleague.admin.adapter.in.web;

import com.paceleague.admin.application.dto.PopupActiveRequest;
import com.paceleague.admin.application.dto.PopupSaveRequest;
import com.paceleague.common.response.ResponseApi;
import com.paceleague.common.web.AdminSno;
import com.paceleague.popup.application.dto.AdminPopupResponse;
import com.paceleague.popup.application.port.in.shared.AdminPopupUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/popups")
@Tag(name = "Admin - Popup", description = "관리자 팝업관리 화면 API — 등록/수정/삭제/활성화. 기간은 KST.")
@SecurityRequirement(name = "adminSession")
public class AdminPopupController {

    private final AdminPopupUseCase adminPopupUseCase;

    public AdminPopupController(AdminPopupUseCase adminPopupUseCase) {
        this.adminPopupUseCase = adminPopupUseCase;
    }

    @Operation(summary = "팝업 목록", description = "최신 등록순 페이지네이션. visibleNow = 지금 실제로 노출 중인지.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseApi<Page<AdminPopupResponse>> list(
            @AdminSno Long adminSno,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseApi.success(adminPopupUseCase.list(page, size));
    }

    @Operation(summary = "팝업 상세")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{popupSno}")
    public ResponseApi<AdminPopupResponse> get(@AdminSno Long adminSno, @PathVariable Long popupSno) {
        return ResponseApi.success(adminPopupUseCase.get(popupSno));
    }

    @Operation(summary = "팝업 등록", description = "target: WEB/APP/BOTH, startAt < endAt 필수, content 는 게시글 에디터 HTML(서버에서 정제).")
    @ApiResponse(responseCode = "200", description = "등록 성공")
    @PostMapping
    public ResponseApi<AdminPopupResponse> create(@AdminSno Long adminSno, @RequestBody PopupSaveRequest req) {
        return ResponseApi.success("저장되었습니다.", adminPopupUseCase.create(req.toCommand()));
    }

    @Operation(summary = "팝업 수정")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @PutMapping("/{popupSno}")
    public ResponseApi<AdminPopupResponse> update(
            @AdminSno Long adminSno,
            @PathVariable Long popupSno,
            @RequestBody PopupSaveRequest req
    ) {
        return ResponseApi.success("저장되었습니다.", adminPopupUseCase.update(popupSno, req.toCommand()));
    }

    @Operation(summary = "팝업 활성화/비활성화", description = "목록에서 바로 켜고 끄는 용도.")
    @ApiResponse(responseCode = "200", description = "변경 성공")
    @PutMapping("/{popupSno}/active")
    public ResponseApi<AdminPopupResponse> changeActive(
            @AdminSno Long adminSno,
            @PathVariable Long popupSno,
            @Valid @RequestBody PopupActiveRequest req
    ) {
        return ResponseApi.success(adminPopupUseCase.changeActive(popupSno, req.active()));
    }

    @Operation(summary = "팝업 삭제")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @DeleteMapping("/{popupSno}")
    public ResponseApi<Void> delete(@AdminSno Long adminSno, @PathVariable Long popupSno) {
        adminPopupUseCase.delete(popupSno);
        return ResponseApi.success("삭제되었습니다.", null);
    }
}
