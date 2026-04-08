package com.example.paceleague.record.service;

import com.example.paceleague.record.dto.RecordMonthResponse;
import org.springframework.data.domain.Page;
import com.example.paceleague.record.entity.Record;
import java.math.BigDecimal;

public interface RecordQueryService {
    Record getOne(Long uno, Long sno);

    Page<Record> getPage(Long uno, int page, int size);

    RecordMonthResponse getMonthAll(Long uno, int year, int month, BigDecimal weightKg);
}
