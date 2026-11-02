package com.paceleague.coupon.application.port.out;

import java.time.Duration;

// 쿠폰 이미지 저장소(S3 비공개 경로). 업로드는 브라우저가 presigned PUT 으로 직접 올린다.
public interface CouponImageStoragePort {

    String presignUpload(String key, String mimeType, Duration expiry);

    String presignDownload(String key, Duration expiry);

    // 객체가 없으면 -1.
    long sizeOf(String key);

    void delete(String key);
}
