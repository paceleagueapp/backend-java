package com.example.paceleague.record.service;

import com.example.paceleague.record.dto.RecordCreateRequest;
import com.example.paceleague.record.repository.RecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.paceleague.record.entity.Record;

import java.util.List;

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

    @Transactional
    public List<Long> createBulk(Long uno, List<RecordCreateRequest> reqList) {
        if (uno == null || uno <= 0) throw new IllegalArgumentException("uno is invalid");
        if (reqList == null || reqList.isEmpty()) throw new IllegalArgumentException("records is empty");

        // 요청 개수 제한(안전장치) - 필요 없으면 제거
        if (reqList.size() > 200) throw new IllegalArgumentException("too many records (max 200)");

        List<Record> entities = reqList.stream().map(req -> {
            if (req.distanceRecord() == null) throw new IllegalArgumentException("distanceRecord is required");
            if (req.startTime() == null) throw new IllegalArgumentException("startTime is required");
            if (req.endTime() == null) throw new IllegalArgumentException("endTime is required");
            if (req.endTime().isBefore(req.startTime())) throw new IllegalArgumentException("endTime must be after startTime");

            return Record.create(
                    uno,
                    req.distanceRecord(),
                    req.startTime(),
                    req.endTime()
            );
        }).toList();

        // saveAll이 내부적으로 배치로 처리될 수 있음(설정에 따라)
        List<Record> saved = recordRepository.saveAll(entities);

        return saved.stream().map(Record::getSno).toList();
    }
}
