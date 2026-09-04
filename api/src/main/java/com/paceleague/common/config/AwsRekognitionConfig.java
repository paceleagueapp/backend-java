package com.paceleague.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;

@Configuration
public class AwsRekognitionConfig {

    // 자격증명은 AwsTranslateConfig와 동일하게 기본 자격증명 체인(운영은 EC2 인스턴스 프로필 paceleague-s3-read role,
    // DetectModerationLabels/StartContentModeration/GetContentModeration 권한이 추가로 필요).
    @Bean
    public RekognitionClient rekognitionClient() {
        return RekognitionClient.builder()
                .region(Region.AP_NORTHEAST_2)
                .build();
    }
}
