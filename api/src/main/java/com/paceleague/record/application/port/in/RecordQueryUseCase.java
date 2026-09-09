package com.paceleague.record.application.port.in;

import com.paceleague.record.application.dto.RecordGpsTrackResponse;
import com.paceleague.record.application.dto.RecordListItemResponse;
import com.paceleague.record.application.dto.RecordMonthResponse;
import com.paceleague.record.application.dto.RunningRecordResponse;
import com.paceleague.record.domain.entity.Record;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface RecordQueryUseCase {
    Record getOne(Long uno, Long sno);

    Page<Record> getPage(Long uno, int page, int size);

    RecordMonthResponse getMonthAll(Long uno, int year, int month, BigDecimal weightKg);

    List<RunningRecordResponse> getRecent30DaysRecords(Long uno);

    // 회원 본인의 러닝 기록 전체(최신순). 각 항목은 GPS 트랙 보유 여부를 함께 내려준다.
    List<RecordListItemResponse> listMyRecords(Long uno);

    // 러닝 1건의 GPS 트랙 전체. 본인 소유가 아니거나 GPS 없이 저장된 러닝이면 IllegalArgumentException(400).
    RecordGpsTrackResponse getGpsTrack(Long uno, Long recordSno);
}
