package com.paceleague.common.i18n;

import java.util.Map;

// ISO 3166-1 alpha-2 국가코드로 Language(10개 지원 언어 중 하나)를 결정한다.
// 매핑에 없는 국가(영어권 대부분 포함)나 null/빈 값은 EN을 기본값으로 반환한다 —
// Language.fromCode(String)이 미지원 언어 코드에 KO로 폴백하는 것과는 별개의 기본값 선택인데,
// 이건 "브라우저 언어 미지원 시 한국어로" 폴백하는 웹 UI(getLang())와 달리
// "국가 미지원 시 국제 공용어인 영어로" 폴백하는 편이 자연스럽기 때문이다.
public class CountryLanguageResolver {

    private static final Language DEFAULT_LANGUAGE = Language.EN;
    private static final Map<String, Language> TABLE = build();

    public static Language resolve(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return DEFAULT_LANGUAGE;
        }
        return TABLE.getOrDefault(countryCode.toUpperCase(), DEFAULT_LANGUAGE);
    }

    private static Map<String, Language> build() {
        return Map.ofEntries(
                Map.entry("KR", Language.KO),

                Map.entry("JP", Language.JA),

                Map.entry("CN", Language.ZH),
                Map.entry("TW", Language.ZH),
                Map.entry("HK", Language.ZH),
                Map.entry("MO", Language.ZH),

                Map.entry("ES", Language.ES),
                Map.entry("MX", Language.ES),
                Map.entry("AR", Language.ES),
                Map.entry("CO", Language.ES),
                Map.entry("PE", Language.ES),
                Map.entry("VE", Language.ES),
                Map.entry("CL", Language.ES),
                Map.entry("EC", Language.ES),
                Map.entry("GT", Language.ES),
                Map.entry("CU", Language.ES),
                Map.entry("BO", Language.ES),
                Map.entry("DO", Language.ES),
                Map.entry("HN", Language.ES),
                Map.entry("PY", Language.ES),
                Map.entry("SV", Language.ES),
                Map.entry("NI", Language.ES),
                Map.entry("CR", Language.ES),
                Map.entry("PA", Language.ES),
                Map.entry("UY", Language.ES),

                Map.entry("FR", Language.FR),
                Map.entry("BE", Language.FR),
                Map.entry("LU", Language.FR),
                Map.entry("MC", Language.FR),
                Map.entry("CI", Language.FR),
                Map.entry("SN", Language.FR),
                Map.entry("ML", Language.FR),
                Map.entry("BF", Language.FR),
                Map.entry("NE", Language.FR),
                Map.entry("TG", Language.FR),
                Map.entry("BJ", Language.FR),
                Map.entry("CD", Language.FR),
                Map.entry("CG", Language.FR),
                Map.entry("GA", Language.FR),
                Map.entry("HT", Language.FR),

                Map.entry("DE", Language.DE),
                Map.entry("AT", Language.DE),
                Map.entry("CH", Language.DE),
                Map.entry("LI", Language.DE),

                Map.entry("PT", Language.PT),
                Map.entry("BR", Language.PT),
                Map.entry("AO", Language.PT),
                Map.entry("MZ", Language.PT),
                Map.entry("CV", Language.PT),
                Map.entry("GW", Language.PT),
                Map.entry("ST", Language.PT),
                Map.entry("TL", Language.PT),

                Map.entry("VN", Language.VI),

                Map.entry("TH", Language.TH)
        );
    }
}
