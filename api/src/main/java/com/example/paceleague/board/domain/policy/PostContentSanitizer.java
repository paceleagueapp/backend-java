package com.example.paceleague.board.domain.policy;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// 게시글 본문 에디터가 굵게/기울임/링크/이미지/동영상만 지원하므로, 번들(Sanitizers.FORMATTING 등)이 허용하는
// 훨씬 넓은 태그 집합 대신 필요한 태그만 직접 화이트리스트한다(video는 어차피 번들에 없음).
// 명시적으로 허용하지 않은 태그/속성(script, style, on*, class, javascript: URL 등)은 라이브러리 기본 동작으로 전부 제거된다.
public final class PostContentSanitizer {

    private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
            .allowElements("p", "br", "div", "b", "strong", "i", "em", "a", "img", "video")
            .allowUrlProtocols("http", "https")
            .allowAttributes("href").onElements("a")
            .requireRelNofollowOnLinks()
            .allowAttributes("src", "alt").onElements("img")
            .allowAttributes("src", "controls").onElements("video")
            .toFactory();

    private static final Pattern TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern IMG_SRC_PATTERN = Pattern.compile("<img[^>]*\\ssrc=\"([^\"]*)\"");
    private static final Pattern VIDEO_SRC_PATTERN = Pattern.compile("<video[^>]*\\ssrc=\"([^\"]*)\"");

    private PostContentSanitizer() {
    }

    public static String sanitize(String rawHtml) {
        return rawHtml == null ? null : POLICY.sanitize(rawHtml);
    }

    // sanitize()를 이미 거친(허용 태그만 남은) HTML에만 쓰는 보조 변환 — 공격자 HTML을 직접 다루는
    // 진짜 방어 경로가 아니라 정규식 수준으로 충분함. "본문이 실질적으로 비어있는지" 판단과
    // AWS Translate용 평문 추출, 두 곳에서만 쓴다.
    public static String toPlainText(String sanitizedHtml) {
        if (sanitizedHtml == null) {
            return "";
        }
        String withoutTags = TAG_PATTERN.matcher(sanitizedHtml).replaceAll(" ");
        String unescaped = withoutTags
                .replace("&nbsp;", " ").replace("&amp;", "&")
                .replace("&lt;", "<").replace("&gt;", ">")
                .replace("&#39;", "'").replace("&quot;", "\"");
        return WHITESPACE_PATTERN.matcher(unescaped).replaceAll(" ").trim();
    }

    public static boolean containsMedia(String sanitizedHtml) {
        return sanitizedHtml != null && (sanitizedHtml.contains("<img") || sanitizedHtml.contains("<video"));
    }

    public record MediaPreview(String type, String url) {
    }

    // 게시글 목록에서 썸네일로 보여줄 첫 이미지/동영상을 본문 HTML에서 찾는다(등장 순서 기준, 둘 다 있으면 먼저 나온 쪽).
    // sanitize()를 이미 거친 HTML만 대상으로 하므로 정규식으로 src만 뽑아내는 정도로 충분하다.
    public static MediaPreview firstMediaPreview(String sanitizedHtml) {
        if (sanitizedHtml == null) {
            return null;
        }
        int imgIndex = sanitizedHtml.indexOf("<img");
        int videoIndex = sanitizedHtml.indexOf("<video");
        if (imgIndex == -1 && videoIndex == -1) {
            return null;
        }

        boolean imageFirst = imgIndex != -1 && (videoIndex == -1 || imgIndex < videoIndex);
        Pattern pattern = imageFirst ? IMG_SRC_PATTERN : VIDEO_SRC_PATTERN;
        Matcher matcher = pattern.matcher(sanitizedHtml);
        if (!matcher.find()) {
            return null;
        }
        return new MediaPreview(imageFirst ? "IMAGE" : "VIDEO", matcher.group(1));
    }
}
