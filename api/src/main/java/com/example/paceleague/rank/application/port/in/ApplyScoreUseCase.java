package com.example.paceleague.rank.application.port.in;

import com.example.paceleague.rank.application.dto.ApplyScoreCommand;

public interface ApplyScoreUseCase {
    void applyScore(ApplyScoreCommand command);
}
