package com.paceleague.common.i18n;

// web/js/i18n.js의 SUPPORTED_LANGUAGES와 동일한 10개 언어. UI 표시용 정적 라벨(티어명/보드명 등)에만 쓰이며,
// board.TranslationServiceImpl의 AWS Translate 대상 언어(자유 텍스트 번역)와는 별개의 관심사다.
public enum Language {
    KO, EN, JA, ZH, ES, FR, DE, PT, VI, TH;

    public static Language fromCode(String code) {
        if (code == null) {
            return KO;
        }
        return switch (code.toLowerCase()) {
            case "en" -> EN;
            case "ja" -> JA;
            case "zh" -> ZH;
            case "es" -> ES;
            case "fr" -> FR;
            case "de" -> DE;
            case "pt" -> PT;
            case "vi" -> VI;
            case "th" -> TH;
            default -> KO;
        };
    }

    public String toCode() {
        return name().toLowerCase();
    }
}
