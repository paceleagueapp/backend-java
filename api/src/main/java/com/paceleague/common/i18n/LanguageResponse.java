package com.paceleague.common.i18n;

public record LanguageResponse(String language) {
    public static LanguageResponse of(Language language) {
        return new LanguageResponse(language.toCode());
    }
}
