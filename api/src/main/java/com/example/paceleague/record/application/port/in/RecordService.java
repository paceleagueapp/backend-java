package com.example.paceleague.record.application.port.in;

import com.example.paceleague.record.application.dto.RecordCreateRequest;

import java.util.List;

public interface RecordService {
    Long create(Long uno, RecordCreateRequest req);

    List<Long> createBulk(Long uno, List<RecordCreateRequest> reqList);
}
