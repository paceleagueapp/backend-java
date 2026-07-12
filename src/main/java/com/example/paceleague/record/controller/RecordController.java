package com.example.paceleague.record.controller;

import com.example.paceleague.common.response.ResponseApi;
import com.example.paceleague.common.security.JwtAuthenticationFilter;
import com.example.paceleague.record.dto.*;
import com.example.paceleague.record.service.RecordQueryService;
import com.example.paceleague.record.service.RecordService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/record")
@Tag(name = "Record", description = "러닝 기록 저장/조회 API")
@SecurityRequirement(name = "bearerAuth")
public class RecordController {
    private final RecordService recordService;
    private final RecordQueryService recordQueryService;

    public RecordController(RecordService recordService, RecordQueryService recordQueryService) {

        this.recordService = recordService;
        this.recordQueryService = recordQueryService;

    }

    // 단건 저장
    @Operation(summary = "기록 단건 저장", description = "러닝 기록 1건을 저장하고 점수를 산정합니다. 최근 1개월 내 동일 startTime이 있으면 저장을 거부합니다.")
    @ApiResponse(responseCode = "200", description = "저장 성공")
    @PostMapping("/save")
    public ResponseEntity<ResponseApi<RecordCreateResponse>> create(Authentication authentication,
                                                                   @RequestBody RecordCreateRequest req) {

        Long sno = recordService.create(uno(authentication), req);
        RecordCreateResponse response = new RecordCreateResponse(sno);

        return ResponseEntity.ok(ResponseApi.success("기록이 저장되었습니다.", response));
    }

    // 여러 건 저장 (배열로 받음)
    @Operation(summary = "기록 일괄 저장", description = "러닝 기록을 배열로 최대 200건까지 저장합니다. 최근 1개월 내 startTime이 중복되는 항목은 조용히 스킵됩니다.")
    @ApiResponse(responseCode = "200", description = "일괄 저장 성공 (스킵된 건 제외)")
    @PostMapping("/bulk")
    public ResponseEntity<ResponseApi<BulkCreateResponse>> createBulk(Authentication authentication,
                                                         @RequestBody List<RecordCreateRequest> reqList) {
        List<Long> ids = recordService.createBulk(uno(authentication), reqList);
        BulkCreateResponse response = new BulkCreateResponse(ids);

        return ResponseEntity.ok(ResponseApi.success("기록이 일괄 저장되었습니다.", response));
    }

    public record BulkCreateResponse(List<Long> savedSnos) {}

    private long uno(Authentication authentication) {
        var p = (JwtAuthenticationFilter.AuthPrincipal) authentication.getPrincipal();
        return p.memberSno();
    }

    // 1) 한 개 조회
    @Operation(summary = "기록 단건 조회", description = "본인 소유 기록 1건을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/dataOne/{sno}")
    public ResponseEntity<ResponseApi<RecordResponse>> getOne(Authentication authentication,
                                                                @Parameter(description = "기록 PK") @PathVariable Long sno) {
        var record = recordQueryService.getOne(uno(authentication), sno);
        RecordResponse response = RecordResponse.from(record);

        return ResponseEntity.ok(ResponseApi.success("기록 조회에 성공했습니다.", response));
    }

    // 2) 10개씩 페이징 조회 (기본 size=10)
    @Operation(summary = "기록 페이징 조회", description = "본인 기록을 startTime 내림차순으로 페이징 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/dataPage")
    public ResponseEntity<ResponseApi<Page<RecordResponse>>> getPage(
            Authentication authentication,
            @Parameter(description = "페이지 번호(0-base)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size
    ) {
        var result = recordQueryService.getPage(uno(authentication), page, size)
                .map(RecordResponse::from);
        return ResponseEntity.ok(ResponseApi.success("기록 목록 조회에 성공했습니다.", result));
    }

    // 3) 한 달치 전체 조회
    @Operation(summary = "월간 기록 전체 조회", description = "지정한 연/월의 기록 목록과, 전체 기간/해당 월 요약(거리·페이스·칼로리)을 함께 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/dataMonth")
    public ResponseEntity<ResponseApi<RecordMonthResponse>> getMonthAll(
            Authentication authentication,
            @Parameter(description = "연도") @RequestParam int year,
            @Parameter(description = "월(1~12)") @RequestParam int month
    ) {
        long uno = uno(authentication);
        BigDecimal weightKg = BigDecimal.valueOf(70); // TODO 실제 회원 체중 조회

        RecordMonthResponse response = recordQueryService.getMonthAll(uno, year, month, weightKg);

        return ResponseEntity.ok(ResponseApi.success("월간 기록 조회에 성공했습니다.", response));
    }

    @Operation(summary = "최근 30일 기록 조회", description = "오늘 기준 최근 30일간의 기록을 최신순으로 반환합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/recent-30-days")
    public ResponseApi<List<RunningRecordResponse>> getRecent30DaysRecords(
            Authentication authentication
    ) {
        long uno = uno(authentication);
        List<RunningRecordResponse> response =
                recordQueryService.getRecent30DaysRecords(uno);

        return ResponseApi.success(response);
    }
}
