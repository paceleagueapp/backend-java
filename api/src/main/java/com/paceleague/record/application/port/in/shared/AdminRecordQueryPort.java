package com.paceleague.record.application.port.in.shared;

import com.paceleague.record.application.dto.AdminRecordSummary;
import org.springframework.data.domain.Page;

// admin 도메인(관리자 웹패널 러닝데이터관리 화면)이 전체 회원의 러닝 기록을 페이지네이션 조회하기 위한 포트.
public interface AdminRecordQueryPort {
    Page<AdminRecordSummary> listRecords(int page, int size);
}
