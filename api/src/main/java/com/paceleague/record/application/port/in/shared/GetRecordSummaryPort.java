package com.paceleague.record.application.port.in.shared;

import com.paceleague.record.application.dto.RunningRecordResponse;

import java.util.Optional;

public interface GetRecordSummaryPort {
    Optional<RunningRecordResponse> getSummary(Long recordSno);
}
