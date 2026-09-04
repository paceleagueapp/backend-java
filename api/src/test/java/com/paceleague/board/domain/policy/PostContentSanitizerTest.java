package com.paceleague.board.domain.policy;

import com.paceleague.board.domain.policy.PostContentSanitizer.MediaPreview;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostContentSanitizerTest {

    @Test
    void null_입력은_null을_반환한다() {
        assertThat(PostContentSanitizer.sanitize(null)).isNull();
    }

    @Test
    void 허용된_태그는_보존된다() {
        String sanitized = PostContentSanitizer.sanitize("<p>Hello <b>World</b></p>");
        assertThat(sanitized).contains("Hello").contains("<b>World</b>");
    }

    @Test
    void script_태그는_제거된다() {
        String sanitized = PostContentSanitizer.sanitize("<script>alert(1)</script><p>safe</p>");
        assertThat(sanitized).doesNotContain("<script").contains("safe");
    }

    @Test
    void on_어트리뷰트는_제거된다() {
        String sanitized = PostContentSanitizer.sanitize("<img src=\"http://x.com/a.png\" onerror=\"alert(1)\">");
        assertThat(sanitized).doesNotContain("onerror").contains("http://x.com/a.png");
    }

    @Test
    void javascript_스킴_링크는_제거된다() {
        String sanitized = PostContentSanitizer.sanitize("<a href=\"javascript:alert(1)\">click</a>");
        assertThat(sanitized).doesNotContain("javascript:");
    }

    @Test
    void class_어트리뷰트는_허용목록에_없어_제거된다() {
        String sanitized = PostContentSanitizer.sanitize("<p class=\"foo\">text</p>");
        assertThat(sanitized).doesNotContain("class=");
    }

    @Test
    void video_태그는_src와_controls_속성과_함께_보존된다() {
        String sanitized = PostContentSanitizer.sanitize("<video src=\"http://x.com/v.mp4\" controls></video>");
        assertThat(sanitized).contains("<video").contains("http://x.com/v.mp4").contains("controls");
    }

    @Test
    void toPlainText는_태그를_제거하고_엔티티를_복원한다() {
        assertThat(PostContentSanitizer.toPlainText("<p>A &amp; B</p>")).isEqualTo("A & B");
    }

    @Test
    void toPlainText는_null이면_빈문자열이다() {
        assertThat(PostContentSanitizer.toPlainText(null)).isEmpty();
    }

    @Test
    void containsMedia는_이미지나_동영상_태그가_있을때만_true다() {
        assertThat(PostContentSanitizer.containsMedia("<p>text</p>")).isFalse();
        assertThat(PostContentSanitizer.containsMedia("<p>text</p><img src=\"x\">")).isTrue();
    }

    @Test
    void snippet은_200자를_넘으면_말줄임표를_붙인다() {
        String longText = "가".repeat(250);
        String snippet = PostContentSanitizer.snippet("<p>" + longText + "</p>");
        assertThat(snippet).hasSize(201).endsWith("…");
    }

    @Test
    void snippet은_텍스트가_없는_미디어_전용_글이면_빈문자열이다() {
        assertThat(PostContentSanitizer.snippet("<img src=\"x\">")).isEmpty();
    }

    @Test
    void firstMediaPreview는_먼저_등장한_미디어를_반환한다() {
        MediaPreview preview = PostContentSanitizer.firstMediaPreview(
                "<p>hi</p><video src=\"v.mp4\" controls></video><img src=\"a.png\">");
        assertThat(preview.type()).isEqualTo("VIDEO");
        assertThat(preview.url()).isEqualTo("v.mp4");
    }

    @Test
    void firstMediaPreview는_미디어가_없으면_null이다() {
        assertThat(PostContentSanitizer.firstMediaPreview("<p>text only</p>")).isNull();
    }
}
