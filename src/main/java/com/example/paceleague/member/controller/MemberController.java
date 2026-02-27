package com.example.paceleague.member.controller;

import com.example.paceleague.member.dto.JoinRequest;
import com.example.paceleague.member.dto.LoginRequest;
import com.example.paceleague.member.dto.TokenResponse;
import com.example.paceleague.member.service.MemberAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
public class MemberController {
    private final MemberAuthService authService;

    public MemberController(MemberAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/join")
    public ResponseEntity<Void> join(@Valid @RequestBody JoinRequest req) {
        authService.join(req.memberId(), req.password(), req.name(), req.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req.memberId(), req.password()));
    }
}
