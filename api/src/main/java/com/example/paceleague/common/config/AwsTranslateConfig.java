package com.example.paceleague.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.translate.TranslateClient;

@Configuration
public class AwsTranslateConfig {

    // 자격증명은 기본 자격증명 체인을 그대로 쓴다 — 운영은 EC2 인스턴스 프로필(paceleague-s3-read role에
    // TranslateReadOnly 권한 추가 필요), 별도 액세스 키를 코드/설정에 넣지 않는다.
    @Bean
    public TranslateClient translateClient() {
        return TranslateClient.builder()
                .region(Region.AP_NORTHEAST_2)
                .build();
    }
}
