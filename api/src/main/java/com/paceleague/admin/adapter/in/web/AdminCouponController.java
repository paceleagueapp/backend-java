package com.paceleague.admin.adapter.in.web;

import com.paceleague.admin.application.dto.CouponGrantRequest;
import com.paceleague.admin.application.dto.CouponImageUploadRequest;
import com.paceleague.common.response.ResponseApi;
import com.paceleague.common.web.AdminSno;
import com.paceleague.coupon.application.dto.CouponImageUploadResponse;
import com.paceleague.coupon.application.dto.CouponResponse;
import com.paceleague.coupon.application.port.in.shared.AdminCouponUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/coupons")
@Tag(name = "Admin - Coupon", description = "관리자 회원 상세 화면의 기프티콘 쿠폰 지급/조회/삭제")
@SecurityRequirement(name = "adminSession")
public class AdminCouponController {

    private final AdminCouponUseCase adminCouponUseCase;

    public AdminCouponController(AdminCouponUseCase adminCouponUseCase) {
        this.adminCouponUseCase = adminCouponUseCase;
    }

    @Operation(summary = "쿠폰 이미지 업로드 URL 발급",
            description = "JPEG/PNG/WEBP, 10MB 이하. 브라우저가 uploadUrl 로 PUT(Content-Type 일치) 한 뒤 imageKey 로 지급 요청.")
    @ApiResponse(responseCode = "200", description = "발급 성공")
    @PostMapping("/upload-url")
    public ResponseApi<CouponImageUploadResponse> requestImageUpload(@AdminSno Long adminSno,
                                                                     @Valid @RequestBody CouponImageUploadRequest req) {
        return ResponseApi.success(adminCouponUseCase.requestImageUpload(req.mimeType(), req.fileSizeBytes()));
    }

    @Operation(summary = "쿠폰 지급", description = "활성 회원 1명에게 지급하고, 받을 수 있으면 개인 푸시를 보냅니다.")
    @ApiResponse(responseCode = "200", description = "지급 성공")
    @PostMapping
    public ResponseApi<CouponResponse> grant(@AdminSno Long adminSno, @Valid @RequestBody CouponGrantRequest req) {
        return ResponseApi.success(adminCouponUseCase.grant(adminSno, req.memberSno(), req.name(), req.expiresAt(), req.imageKey()));
    }

    @Operation(summary = "회원의 쿠폰 목록", description = "최근 지급순. imageUrl 은 10분간 유효.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseApi<List<CouponResponse>> listByMember(@AdminSno Long adminSno, @RequestParam Long memberSno) {
        return ResponseApi.success(adminCouponUseCase.listByMember(memberSno));
    }

    @Operation(summary = "쿠폰 삭제(회수)", description = "잘못 지급한 쿠폰을 지웁니다. 이미지도 함께 삭제.")
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @DeleteMapping("/{couponSno}")
    public ResponseApi<Void> delete(@AdminSno Long adminSno, @PathVariable Long couponSno) {
        adminCouponUseCase.delete(couponSno);
        return ResponseApi.success("삭제되었습니다.", null);
    }
}
