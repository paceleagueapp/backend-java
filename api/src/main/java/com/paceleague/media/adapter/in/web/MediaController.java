package com.paceleague.media.adapter.in.web;

import com.paceleague.common.response.ResponseApi;
import com.paceleague.common.web.MemberSno;
import com.paceleague.media.application.dto.CreateLinkRequest;
import com.paceleague.media.application.dto.MediaStatusResponse;
import com.paceleague.media.application.dto.MediaUploadInitResponse;
import com.paceleague.media.application.dto.MediaUploadRequest;
import com.paceleague.media.application.port.in.MediaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/media")
@Tag(name = "Media", description = "게시글 첨부(이미지/동영상/링크) API — S3 presigned URL 업로드 + AWS Rekognition 콘텐츠 모더레이션. 전부 인증 필요.")
@SecurityRequirement(name = "bearerAuth")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @Operation(summary = "업로드 URL 발급", description = "이미지/동영상 업로드용 presigned PUT URL을 발급합니다. 클라이언트는 반환된 uploadUrl로 파일을 직접 PUT한 뒤 /complete를 호출해야 합니다.")
    @ApiResponse(responseCode = "200", description = "발급 성공")
    @PostMapping("/uploads")
    public ResponseApi<MediaUploadInitResponse> requestUpload(
            @MemberSno Long memberSno,
            @RequestBody MediaUploadRequest req
    ) {
        return ResponseApi.success(mediaService.requestUpload(memberSno, req));
    }

    @Operation(summary = "업로드 완료 처리", description = "S3에 파일 업로드가 끝난 뒤 호출합니다. 이미지는 즉시 모더레이션 결과를 반환하고, 동영상은 비동기 작업만 시작한 뒤 PENDING을 반환합니다(/status로 폴링).")
    @ApiResponse(responseCode = "200", description = "처리 성공(승인/거부/처리중 모두 200)")
    @PostMapping("/{mediaSno}/complete")
    public ResponseApi<MediaStatusResponse> completeUpload(
            @MemberSno Long memberSno,
            @PathVariable Long mediaSno
    ) {
        return ResponseApi.success(mediaService.completeUpload(memberSno, mediaSno));
    }

    @Operation(summary = "업로드/모더레이션 상태 조회", description = "동영상처럼 비동기로 모더레이션 중인 미디어의 진행 상태를 폴링합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/{mediaSno}/status")
    public ResponseApi<MediaStatusResponse> getUploadStatus(
            @MemberSno Long memberSno,
            @PathVariable Long mediaSno
    ) {
        return ResponseApi.success(mediaService.getUploadStatus(memberSno, mediaSno));
    }

    @Operation(summary = "링크 첨부 생성", description = "업로드 없이 URL만 첨부합니다. http(s) URL만 허용되며 모더레이션 대상이 아닙니다.")
    @ApiResponse(responseCode = "200", description = "생성 성공")
    @PostMapping("/links")
    public ResponseApi<Long> createLink(
            @MemberSno Long memberSno,
            @RequestBody CreateLinkRequest req
    ) {
        return ResponseApi.success(mediaService.createLink(memberSno, req.url()));
    }
}
