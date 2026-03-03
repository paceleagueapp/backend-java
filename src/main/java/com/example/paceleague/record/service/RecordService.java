package com.example.paceleague.record.service;

import com.example.paceleague.record.dto.RecordCreateRequest;
import com.example.paceleague.record.repository.RecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.paceleague.record.entity.Record;

@Service
public class RecordService {
    private final RecordRepository recordRepository;

    public RecordService(RecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    @Transactional
    public Long create(Long uno, RecordCreateRequest req) {
        if (uno == null || uno <= 0) {
            throw new IllegalArgumentException("uno is invalid");
        }

        Record record = new Record(
                uno,
                req.distanceRecord(),
                req.startTime(),
                req.endTime()
        );

        return recordRepository.save(record).getSno();
    }
}
