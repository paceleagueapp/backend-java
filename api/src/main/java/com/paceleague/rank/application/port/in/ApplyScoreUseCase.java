package com.paceleague.rank.application.port.in;

import com.paceleague.rank.application.dto.ApplyScoreCommand;

public interface ApplyScoreUseCase {
    void applyScore(ApplyScoreCommand command);
}
