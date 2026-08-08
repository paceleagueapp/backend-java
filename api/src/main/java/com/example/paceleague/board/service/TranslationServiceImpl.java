package com.example.paceleague.board.service;

import com.example.paceleague.board.dto.CommentTranslationResponse;
import com.example.paceleague.board.dto.PostTranslationResponse;
import com.example.paceleague.board.entity.Comment;
import com.example.paceleague.board.entity.Post;
import com.example.paceleague.board.repository.CommentRepository;
import com.example.paceleague.board.repository.PostRepository;
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

    private final TranslateClient translateClient;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public PostTranslationResponse translatePost(Long postSno, String targetLanguage) {
        String lang = requireSupportedLanguage(targetLanguage);
        String cacheKey = "translate:post:" + postSno + ":" + lang;

        String cached = redis.opsForValue().get(cacheKey);
        if (cached != null) {
            return readJson(cached, PostTranslationResponse.class);
        }

        Post post = postRepository.findById(postSno)
                .orElseThrow(() -> new IllegalArgumentException("post not found"));

        PostTranslationResponse response = new PostTranslationResponse(
                translateText(post.getTitle(), lang),
                translateText(post.getContent(), lang)
        );

        redis.opsForValue().set(cacheKey, writeJson(response), CACHE_TTL);
        return response;
    }

    public CommentTranslationResponse translateComment(Long commentSno, String targetLanguage) {
        String lang = requireSupportedLanguage(targetLanguage);
        String cacheKey = "translate:comment:" + commentSno + ":" + lang;

        String cached = redis.opsForValue().get(cacheKey);
        if (cached != null) {
            return readJson(cached, CommentTranslationResponse.class);
        }

        Comment comment = commentRepository.findById(commentSno)
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
}
