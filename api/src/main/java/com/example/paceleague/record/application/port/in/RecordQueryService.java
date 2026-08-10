package com.example.paceleague.record.application.port.in;

import com.example.paceleague.record.application.dto.RecordMonthResponse;
import com.example.paceleague.record.application.dto.RunningRecordResponse;
import com.example.paceleague.record.domain.entity.Record;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface RecordQueryService {
    Record getOne(Long uno, Long sno);

    Page<Record> getPage(Long uno, int page, int size);

    RecordMonthResponse getMonthAll(Long uno, int year, int month, BigDecimal weightKg);

    List<RunningRecordResponse> getRecent30DaysRecords(Long uno);
}
