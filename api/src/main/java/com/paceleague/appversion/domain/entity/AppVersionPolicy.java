package com.paceleague.appversion.domain.entity;

import com.paceleague.appversion.domain.enums.AppPlatform;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
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
}
