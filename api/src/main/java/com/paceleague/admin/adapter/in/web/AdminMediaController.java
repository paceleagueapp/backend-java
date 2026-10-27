package com.paceleague.admin.adapter.in.web;

import com.paceleague.common.response.ResponseApi;
import com.paceleague.common.web.AdminSno;
import com.paceleague.media.application.dto.MediaStatusResponse;
import com.paceleague.media.application.dto.MediaUploadInitResponse;
import com.paceleague.media.application.dto.MediaUploadRequest;
import com.paceleague.media.application.port.in.MediaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

// 관리자 화면(팝업관리)의 게시글 에디터가 이미지/동영상을 올릴 때 쓰는 경로 — /api/media 와 같은 presigned
// 업로드 + Rekognition 모더레이션 흐름을 그대로 쓰되, 회원 JWT 가 아니라 관리자 세션으로 인증하고
// media.member_sno 에는 MediaUseCase.ADMIN_UPLOADER_SNO(0)를 넣는다.
@RestController
@RequestMapping("/api/admin/media")
@Tag(name = "Admin - Media", description = "관리자 에디터 이미지/동영상 업로드 — /api/media 와 동일한 흐름")
@SecurityRequirement(name = "adminSession")
public class AdminMediaController {

    private final MediaUseCase mediaUseCase;

    public AdminMediaController(MediaUseCase mediaUseCase) {
        this.mediaUseCase = mediaUseCase;
    }

    @Operation(summary = "업로드 URL 발급")
    @ApiResponse(responseCode = "200", description = "발급 성공")
    @PostMapping("/uploads")
    public ResponseApi<MediaUploadInitResponse> requestUpload(@AdminSno Long adminSno, @RequestBody MediaUploadRequest req) {
        return ResponseApi.success(mediaUseCase.requestUpload(MediaUseCase.ADMIN_UPLOADER_SNO, req));
    }

    @Operation(summary = "업로드 완료 처리")
    @ApiResponse(responseCode = "200", description = "처리 성공")
    @PostMapping("/{mediaSno}/complete")
    public ResponseApi<MediaStatusResponse> completeUpload(@AdminSno Long adminSno, @PathVariable Long mediaSno) {
        return ResponseApi.success(mediaUseCase.completeUpload(MediaUseCase.ADMIN_UPLOADER_SNO, mediaSno));
    }

    @Operation(summary = "업로드/모더레이션 상태 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{mediaSno}/status")
    public ResponseApi<MediaStatusResponse> getUploadStatus(@AdminSno Long adminSno, @PathVariable Long mediaSno) {
        return ResponseApi.success(mediaUseCase.getUploadStatus(MediaUseCase.ADMIN_UPLOADER_SNO, mediaSno));
    }
}
