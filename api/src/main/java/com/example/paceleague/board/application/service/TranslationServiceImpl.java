package com.example.paceleague.board.application.service;

import com.example.paceleague.board.application.dto.CommentTranslationResponse;
import com.example.paceleague.board.application.dto.PostTranslationResponse;
import com.example.paceleague.board.application.port.in.TranslationService;
import com.example.paceleague.board.application.port.out.CommentRepositoryPort;
import com.example.paceleague.board.application.port.out.PostRepositoryPort;
import com.example.paceleague.board.domain.entity.Comment;
import com.example.paceleague.board.domain.entity.Post;
import com.example.paceleague.board.domain.policy.PostContentSanitizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TranslationServiceImpl implements TranslationService {

    // UI 언어 선택기와 동일한 10개 언어만 허용 — web/js/i18n.js의 지원 언어 목록과 맞춰서 유지한다.
    private static final Set<String> SUPPORTED_LANGUAGES =
            Set.of("ko", "en", "ja", "zh", "es", "fr", "de", "pt", "vi", "th");
    private static final Duration CACHE_TTL = Duration.ofDays(180);

    // 캐시 미스일 때만 실제 AWS Translate 호출(과금)이 발생하므로, 사용자당 분당 호출 수를 제한해 비용을 통제한다.
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(1);
    private static final long RATE_LIMIT_MAX_CALLS = 20;

    private final TranslateClient translateClient;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final PostRepositoryPort postRepositoryPort;
    private final CommentRepositoryPort commentRepositoryPort;

    public PostTranslationResponse translatePost(Long memberSno, Long postSno, String targetLanguage) {
        String lang = requireSupportedLanguage(targetLanguage);
        String cacheKey = "translate:post:" + postSno + ":" + lang;

        String cached = redis.opsForValue().get(cacheKey);
        if (cached != null) {
            return readJson(cached, PostTranslationResponse.class);
        }

        checkRateLimit(memberSno);

        Post post = postRepositoryPort.findById(postSno)
                .orElseThrow(() -> new IllegalArgumentException("post not found"));

        // AWS Translate는 HTML을 이해하지 못해 태그가 섞인 채로 넘기면 그대로 깨져서 번역되므로,
        // 평문만 추출해 번역한다(이미지/동영상 전용 글처럼 평문이 없으면 호출 자체를 생략).
        String contentPlainText = PostContentSanitizer.toPlainText(post.getContent());
        String translatedContent = contentPlainText.isBlank() ? "" : translateText(contentPlainText, lang);

        PostTranslationResponse response = new PostTranslationResponse(
                translateText(post.getTitle(), lang),
                translatedContent
        );

        redis.opsForValue().set(cacheKey, writeJson(response), CACHE_TTL);
        return response;
    }

    public CommentTranslationResponse translateComment(Long memberSno, Long commentSno, String targetLanguage) {
        String lang = requireSupportedLanguage(targetLanguage);
        String cacheKey = "translate:comment:" + commentSno + ":" + lang;

        String cached = redis.opsForValue().get(cacheKey);
        if (cached != null) {
            return readJson(cached, CommentTranslationResponse.class);
        }

        checkRateLimit(memberSno);

        Comment comment = commentRepositoryPort.findById(commentSno)
                .orElseThrow(() -> new IllegalArgumentException("comment not found"));

        CommentTranslationResponse response = new CommentTranslationResponse(translateText(comment.getContent(), lang));

        redis.opsForValue().set(cacheKey, writeJson(response), CACHE_TTL);
        return response;
    }

    private String translateText(String text, String targetLanguage) {
        TranslateTextRequest request = TranslateTextRequest.builder()
                .text(text)
                .sourceLanguageCode("auto")
                .targetLanguageCode(targetLanguage)
                .build();
        return translateClient.translateText(request).translatedText();
    }

    private String requireSupportedLanguage(String targetLanguage) {
        if (targetLanguage == null || !SUPPORTED_LANGUAGES.contains(targetLanguage)) {
            throw new IllegalArgumentException("unsupported target language: " + targetLanguage);
        }
        return targetLanguage;
    }

    private <T> T readJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalStateException("failed to read cached translation", e);
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("failed to cache translation", e);
        }
    }

    private void checkRateLimit(Long memberSno) {
        String key = "translate:ratelimit:" + memberSno;
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, RATE_LIMIT_WINDOW);
        }
        if (count != null && count > RATE_LIMIT_MAX_CALLS) {
            throw new IllegalArgumentException("번역 요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
        }
    }
}
