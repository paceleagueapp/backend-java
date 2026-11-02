package com.paceleague.coupon.adapter.in.web;

import com.paceleague.common.response.ResponseApi;
import com.paceleague.common.web.MemberSno;
import com.paceleague.coupon.application.dto.CouponResponse;
import com.paceleague.coupon.application.port.in.CouponUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 앱 쿠폰함. 모바일 앱 전용(CORS 없음).
@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
@Tag(name = "Coupon", description = "쿠폰함 — 관리자가 지급한 기프티콘 조회/사용 완료 표시")
public class CouponController {

    private final CouponUseCase couponUseCase;

    @Operation(summary = "내 쿠폰 목록",
            description = "최근 지급순. imageUrl 은 10분간 유효한 presigned URL 이라 화면을 열 때마다 다시 받아야 합니다. "
                    + "expired=true 는 expiresAt(KST) 이 지난 쿠폰.")
    @ApiResponse(responseCode = "200", description = "조회 성공(없으면 빈 배열)")
    @GetMapping("/me")
    public ResponseApi<List<CouponResponse>> listMine(@MemberSno Long memberSno) {
        return ResponseApi.success(couponUseCase.listMine(memberSno));
    }

    @Operation(summary = "쿠폰 사용 완료 표시",
            description = "회원이 직접 사용 완료로 표시합니다. 이미 사용했거나 만료된 쿠폰, 남의 쿠폰이면 400.")
    @ApiResponse(responseCode = "200", description = "처리 성공")
    @PostMapping("/{couponSno}/use")
    public ResponseApi<CouponResponse> markUsed(@MemberSno Long memberSno,
                                                @Parameter(description = "쿠폰 PK") @PathVariable Long couponSno) {
        return ResponseApi.success(couponUseCase.markUsed(memberSno, couponSno));
    }
}
