package com.example.paceleague.record.service;

import com.example.paceleague.record.dto.RecordCreateRequest;

import java.util.List;

public interface RecordService {
    Long create(Long uno, RecordCreateRequest req);

    List<Long> createBulk(Long uno, List<RecordCreateRequest> reqList);
}
