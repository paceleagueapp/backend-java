package com.paceleague.notification.application.port.out;

import com.paceleague.notification.domain.entity.PushSendLog;

import java.time.LocalDate;
import java.util.Optional;

public interface PushSendLogRepositoryPort {

    // (kind, targetDate) 발송 시도를 선점한다.
    //  - 이미 SENT/SKIPPED/RESERVED 행이 있으면 Optional.empty() (다른 실행이 처리 중이거나 이미 처리됨)
    //  - FAILED 행이 있으면 RESERVED 로 되돌리고 그 행을 반환(재시도)
    //  - 없으면 RESERVED 행을 새로 만들어 반환
    // 자체 트랜잭션에서 실행돼 UNIQUE 경쟁(다중 인스턴스)도 안전하게 처리한다.
    Optional<PushSendLog> tryReserve(String kind, LocalDate targetDate);

    PushSendLog save(PushSendLog log);
}
