package com.paceleague.coupon.adapter.out.s3;

import com.paceleague.coupon.application.port.out.CouponImageStoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

// media 와 같은 버킷(paceleague-media)이지만 coupon/ 경로 — 버킷 정책상 media/* 만 공개라 여기는 비공개다.
// EC2 역할에 coupon/* 의 Put/Get/Delete 권한이 있어야 한다(docs/infra.md).
@Component
@RequiredArgsConstructor
public class CouponImageS3Adapter implements CouponImageStoragePort {

    @Value("${app.media.bucket}")
    private String bucket;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Override
    public String presignUpload(String key, String mimeType, Duration expiry) {
        PutObjectRequest put = PutObjectRequest.builder().bucket(bucket).key(key).contentType(mimeType).build();
        return s3Presigner.presignPutObject(PutObjectPresignRequest.builder()
                .signatureDuration(expiry).putObjectRequest(put).build()).url().toString();
    }

    @Override
    public String presignDownload(String key, Duration expiry) {
        GetObjectRequest get = GetObjectRequest.builder().bucket(bucket).key(key).build();
        return s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
                .signatureDuration(expiry).getObjectRequest(get).build()).url().toString();
    }

    @Override
    public long sizeOf(String key) {
        try {
            return s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build()).contentLength();
        } catch (NoSuchKeyException e) {
            return -1;
        }
    }

    @Override
    public void delete(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    }
}
