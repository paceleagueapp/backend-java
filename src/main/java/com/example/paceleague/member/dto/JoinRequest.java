package com.example.paceleague.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinRequest (
        @NotBlank @Size(max = 50) String memberId,
        @NotBlank @Size(min = 4, max = 100) String password,
        @Size(max = 50) String nickname,
        @Size(max = 50) String email
){}
