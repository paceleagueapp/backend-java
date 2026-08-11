package com.example.paceleague.common.i18n;

// board/rank/ranking의 lang 파라미터를 받는 GET 엔드포인트들이 country 파라미터도 함께 받을 수 있게 해주는 공용 리졸버.
// country가 주어지면 country를 우선시하고(둘 다 오면 country가 이긴다), 없으면 lang을 그대로 쓴다.
public class LocaleResolver {

    public static Language resolve(String lang, String country) {
        if (country != null && !country.isBlank()) {
            return CountryLanguageResolver.resolve(country);
        }
        return Language.fromCode(lang);
    }
}
