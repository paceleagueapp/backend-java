package com.paceleague.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AwsS3Config {

    // 자격증명은 AwsTranslateConfig와 동일하게 기본 자격증명 체인(운영은 EC2 인스턴스 프로필 paceleague-s3-read role,
    // media 업로드/조회/삭제 권한이 추가로 필요 — docs/infra.md 참고).
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.AP_NORTHEAST_2)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.AP_NORTHEAST_2)
                .build();
    }
}
