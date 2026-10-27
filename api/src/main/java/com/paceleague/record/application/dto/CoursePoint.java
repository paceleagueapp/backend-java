package com.paceleague.record.application.dto;

// 게시글에 보여줄 러닝 코스(경로) 한 점 — 위경도만 내려준다(시각/고도/정확도 등은 공개하지 않음).
public record CoursePoint(double lat, double lng) {}
