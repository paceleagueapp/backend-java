package com.paceleague.notification.adapter.out.push;

// FCM 발송 자체가 실패했을 때. 스케줄러/서비스에서 잡아 push_send_log 에 FAILED 로 기록하고 삼킨다.
public class PushSendException extends RuntimeException {
    public PushSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
