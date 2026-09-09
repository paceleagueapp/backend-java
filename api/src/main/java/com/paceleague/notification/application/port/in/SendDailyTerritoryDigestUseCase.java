package com.paceleague.notification.application.port.in;

// 매일 아침 "어제 N명이 M개의 땅을 점령했어요" 전체 요약 푸시.
public interface SendDailyTerritoryDigestUseCase {
    void sendForYesterday();
}
