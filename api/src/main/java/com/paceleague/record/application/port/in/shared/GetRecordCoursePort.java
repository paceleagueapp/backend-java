package com.paceleague.record.application.port.in.shared;

import com.paceleague.record.application.dto.CoursePoint;

import java.util.List;

// 코스추천 게시글에 첨부된 러닝의 경로. GPS 트랙이 없는 러닝(수동 저장)이면 빈 목록.
// 소유자 확인 없음 — 작성 시점에 본인 러닝만 첨부할 수 있게 board 쪽에서 이미 검증한다.
public interface GetRecordCoursePort {
    List<CoursePoint> getCourse(Long recordSno);
}
