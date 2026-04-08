package com.example.paceleague.record.controller;

import com.example.paceleague.common.response.ResponseApi;
import com.example.paceleague.common.security.JwtAuthenticationFilter;
import com.example.paceleague.record.dto.RecordCreateRequest;
import com.example.paceleague.record.dto.RecordCreateResponse;
import com.example.paceleague.record.dto.RecordMonthResponse;
import com.example.paceleague.record.dto.RecordResponse;
import com.example.paceleague.record.service.RecordQueryService;
import com.example.paceleague.record.service.RecordService;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/record")
public class RecordController {
    private final RecordService recordService;
    private final RecordQueryService recordQueryService;

    public RecordController(RecordService recordService, RecordQueryService recordQueryService) {

        this.recordService = recordService;
        this.recordQueryService = recordQueryService;

    }

    // 단건 저장
    @PostMapping("/save")
    public ResponseEntity<ResponseApi<RecordCreateResponse>> create(Authentication authentication,
                                                                   @RequestBody RecordCreateRequest req) {

        Long sno = recordService.create(uno(authentication), req);
        RecordCreateResponse response = new RecordCreateResponse(sno);

        return ResponseEntity.ok(ResponseApi.success("기록이 저장되었습니다.", response));
    }

    // 여러 건 저장 (배열로 받음)
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
    @GetMapping("/dataOne/{sno}")
    public ResponseEntity<ResponseApi<RecordResponse>> getOne(Authentication authentication, @PathVariable Long sno) {
        var record = recordQueryService.getOne(uno(authentication), sno);
        RecordResponse response = RecordResponse.from(record);

        return ResponseEntity.ok(ResponseApi.success("기록 조회에 성공했습니다.", response));
    }

    // 2) 10개씩 페이징 조회 (기본 size=10)
    @GetMapping("/dataPage")
    public ResponseEntity<ResponseApi<Page<RecordResponse>>> getPage(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var result = recordQueryService.getPage(uno(authentication), page, size)
                .map(RecordResponse::from);
        return ResponseEntity.ok(ResponseApi.success("기록 목록 조회에 성공했습니다.", result));
    }

    // 3) 한 달치 전체 조회
    @GetMapping("/dataMonth")
    public ResponseEntity<ResponseApi<RecordMonthResponse>> getMonthAll(
            Authentication authentication,
            @RequestParam int year,
            @RequestParam int month
    ) {
        long uno = uno(authentication);
        BigDecimal weightKg = BigDecimal.valueOf(70); // TODO 실제 회원 체중 조회

        RecordMonthResponse response = recordQueryService.getMonthAll(uno, year, month, weightKg);

        return ResponseEntity.ok(ResponseApi.success("월간 기록 조회에 성공했습니다.", response));
    }
}
