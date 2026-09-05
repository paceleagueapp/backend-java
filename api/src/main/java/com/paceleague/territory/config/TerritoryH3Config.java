package com.paceleague.territory.config;

import com.uber.h3core.H3Core;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

// H3Core는 요청마다 만들 대상이 아니다(네이티브 라이브러리를 디스크에 풀어 로드) — 싱글턴 빈으로 등록해
// ProcessTerritoryRunService/TerritoryQueryService 등에 직접 주입한다(StringRedisTemplate과 같은 패턴).
@Configuration
public class TerritoryH3Config {

    @Bean
    public H3Core h3Core() throws IOException {
        return H3Core.newInstance();
    }
}
