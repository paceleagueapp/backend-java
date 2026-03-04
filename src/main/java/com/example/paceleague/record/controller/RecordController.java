package com.example.paceleague.record.controller;

import com.example.paceleague.common.security.JwtAuthenticationFilter;
import com.example.paceleague.record.dto.RecordCreateRequest;
import com.example.paceleague.record.dto.RecordCreateResponse;
import com.example.paceleague.record.dto.RecordResponse;
import com.example.paceleague.record.service.RecordQueryService;
import com.example.paceleague.record.service.RecordService;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<RecordCreateResponse> create(Authentication authentication,
                                                       @RequestBody RecordCreateRequest req) {

        // JWT 필터에서 principal로 넣어둔 AuthPrincipal 사용
        var principal = (JwtAuthenticationFilter.AuthPrincipal) authentication.getPrincipal();

        long uno = principal.memberSno();

        Long sno = recordService.create(uno, req);
        return ResponseEntity.ok(new RecordCreateResponse(sno));
    }

    // 여러 건 저장 (배열로 받음)
    @PostMapping("/bulk")
    public ResponseEntity<BulkCreateResponse> createBulk(Authentication authentication,
                                                         @RequestBody List<RecordCreateRequest> reqList) {
        List<Long> ids = recordService.createBulk(uno(authentication), reqList);
        return ResponseEntity.ok(new BulkCreateResponse(ids));
    }

    public record BulkCreateResponse(List<Long> savedSnos) {}

    private long uno(Authentication authentication) {
        var p = (JwtAuthenticationFilter.AuthPrincipal) authentication.getPrincipal();
        return p.memberSno();
    }

    // 1) 한 개 조회
    @GetMapping("/dataOne/{sno}")
    public ResponseEntity<RecordResponse> getOne(Authentication authentication, @PathVariable Long sno) {
        var record = recordQueryService.getOne(uno(authentication), sno);
        return ResponseEntity.ok(RecordResponse.from(record));
    }

    // 2) 10개씩 페이징 조회 (기본 size=10)
    @GetMapping("/dataPage")
    public ResponseEntity<Page<RecordResponse>> getPage(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var result = recordQueryService.getPage(uno(authentication), page, size)
                .map(RecordResponse::from);
        return ResponseEntity.ok(result);
    }

    // 3) 한 달치 전체 조회
    @GetMapping("/dataMonth")
    public ResponseEntity<List<RecordResponse>> getMonthAll(
            Authentication authentication,
            @RequestParam int year,
            @RequestParam int month
    ) {
        var list = recordQueryService.getMonthAll(uno(authentication), year, month)
                .stream()
                .map(RecordResponse::from)
                .toList();

        return ResponseEntity.ok(list);
    }
}
