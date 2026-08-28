package com.example.paceleague.crew.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// paceleague.crew.* 튜닝 값. app.jwt(JwtProperties)/paceleague.territory(TerritoryProperties)와 같은 방식.
// application*.yml에는 넣지 않고 아래 기본값을 그대로 쓴다.
@ConfigurationProperties(prefix = "paceleague.crew")
public record CrewProperties(
        Integer nameMinLength,
        Integer nameMaxLength,
        Integer descriptionMaxLength,
        Integer noticeMaxLength,
        Integer memberLimitDefault,
        Integer invitationExpireDays,
        Integer searchMaxResults
) {
    public CrewProperties {
        if (nameMinLength == null) nameMinLength = 2;
        if (nameMaxLength == null) nameMaxLength = 20;
        if (descriptionMaxLength == null) descriptionMaxLength = 500;
        if (noticeMaxLength == null) noticeMaxLength = 2000;
        if (memberLimitDefault == null) memberLimitDefault = 30;
        if (invitationExpireDays == null) invitationExpireDays = 7;
        if (searchMaxResults == null) searchMaxResults = 30;
    }
}
