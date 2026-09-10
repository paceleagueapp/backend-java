package com.paceleague.member.application.port.in.shared;

import com.paceleague.member.application.dto.AdminMemberDetail;
import com.paceleague.member.application.dto.AdminMemberSummary;
import org.springframework.data.domain.Page;

// admin 도메인(관리자 웹패널 회원관리 화면)이 회원 목록/상세를 조회하기 위한 크로스 도메인 포트.
public interface AdminMemberQueryPort {
    Page<AdminMemberSummary> search(String query, int page, int size);

    AdminMemberDetail getDetail(Long memberSno);
}
