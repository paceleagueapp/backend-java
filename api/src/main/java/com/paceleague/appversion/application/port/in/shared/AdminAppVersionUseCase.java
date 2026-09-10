package com.paceleague.appversion.application.port.in.shared;

import com.paceleague.appversion.application.dto.AppVersionSummary;
import com.paceleague.appversion.domain.enums.AppPlatform;

import java.util.List;

// admin 도메인(관리자 웹패널 설정 화면)이 안드로이드/iOS 버전 정책을 조회·수정하기 위한 포트.
// 설정 화면 범위는 "버전 설정"만이라 storeUrl/updateMessage/유지보수 필드는 다루지 않는다.
public interface AdminAppVersionUseCase {
    List<AppVersionSummary> list();

    AppVersionSummary updateVersion(AppPlatform platform, String latestVersion, String minRequiredVersion);
}
