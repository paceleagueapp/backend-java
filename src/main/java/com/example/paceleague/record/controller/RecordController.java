package com.example.paceleague.record.controller;

import com.example.paceleague.common.security.JwtAuthenticationFilter;
import com.example.paceleague.record.dto.RecordCreateRequest;
import com.example.paceleague.record.dto.RecordCreateResponse;
import com.example.paceleague.record.service.RecordService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/record")
public class RecordController {
    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @PostMapping("/save")
    public ResponseEntity<RecordCreateResponse> create(Authentication authentication,
                                                       @RequestBody RecordCreateRequest req) {

        // JWT 필터에서 principal로 넣어둔 AuthPrincipal 사용
        var principal = (JwtAuthenticationFilter.AuthPrincipal) authentication.getPrincipal();

        long uno = principal.memberSno();

        Long sno = recordService.create(uno, req);
        return ResponseEntity.ok(new RecordCreateResponse(sno));
    }
}
