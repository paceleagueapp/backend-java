package com.paceleague.appversion.domain.entity;

import com.paceleague.appversion.domain.enums.AppPlatform;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "app_version_policy")
public class AppVersionPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sno;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppPlatform platform;

    @Column(name = "latest_version", nullable = false, length = 30)
    private String latestVersion;

    @Column(name = "min_required_version", nullable = false, length = 30)
    private String minRequiredVersion;

    @Column(name = "store_url", length = 500)
    private String storeUrl;

    @Column(name = "update_message", length = 500)
    private String updateMessage;

    @Column(name = "maintenance_yn", length = 1)
    private String maintenanceYn;

    @Column(name = "maintenance_message", length = 500)
    private String maintenanceMessage;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    private AppVersionPolicy(AppPlatform platform, String latestVersion, String minRequiredVersion) {
        this.platform = platform;
        this.latestVersion = latestVersion;
        this.minRequiredVersion = minRequiredVersion;
        this.createAt = LocalDateTime.now();
        this.updateAt = this.createAt;
    }

    // 관리자 설정 화면에서 처음 이 플랫폼의 버전을 지정할 때 — storeUrl/updateMessage/maintenance 등은
    // 아직 관리 대상이 아니라(설정 화면에는 버전 두 필드만 존재) null로 남겨둔다.
    public static AppVersionPolicy create(AppPlatform platform, String latestVersion, String minRequiredVersion) {
        return new AppVersionPolicy(platform, latestVersion, minRequiredVersion);
    }

    public void updateVersion(String latestVersion, String minRequiredVersion) {
        this.latestVersion = latestVersion;
        this.minRequiredVersion = minRequiredVersion;
        this.updateAt = LocalDateTime.now();
    }
}
