package com.example.paceleague.board.domain.policy;

import com.example.paceleague.common.i18n.Language;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoardLabelPolicyTest {

    @Test
    void 한국어는_항상_fallback_값을_그대로_반환한다() {
        assertThat(BoardLabelPolicy.name("free", Language.KO, "자유게시판")).isEqualTo("자유게시판");
        assertThat(BoardLabelPolicy.description("free", Language.KO, "자유롭게 이야기하는 공간")).isEqualTo("자유롭게 이야기하는 공간");
    }

    @Test
    void 등록된_슬러그와_언어면_번역된_이름과_설명을_반환한다() {
        assertThat(BoardLabelPolicy.name("free", Language.EN, "fallback")).isEqualTo("Free Talk");
        assertThat(BoardLabelPolicy.description("qna", Language.JA, "fallback"))
                .isEqualTo("ランニングに関する質問を投稿してください。");
    }

    @Test
    void 등록되지_않은_슬러그는_fallback_값을_반환한다() {
        assertThat(BoardLabelPolicy.name("unknown-board", Language.EN, "fallback")).isEqualTo("fallback");
        assertThat(BoardLabelPolicy.description("unknown-board", Language.EN, "fallback")).isEqualTo("fallback");
    }

    @Test
    void 등록된_슬러그의_모든_비한국어_언어에_라벨이_존재한다() {
        for (Language lang : Language.values()) {
            if (lang == Language.KO) {
                continue;
            }
            for (String slug : new String[] {"free", "qna", "verify", "crew_promo"}) {
                assertThat(BoardLabelPolicy.name(slug, lang, "fallback"))
                        .as("slug=%s lang=%s", slug, lang)
                        .isNotEqualTo("fallback");
            }
        }
    }
}
