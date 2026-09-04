package com.paceleague.media.domain.entity;

import com.paceleague.media.domain.enums.MediaStatus;
import com.paceleague.media.domain.enums.MediaType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "media")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sno;

    @Column(name = "member_sno", nullable = false)
    private Long memberSno;

    @Column(name = "post_sno")
    private Long postSno;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MediaType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MediaStatus status;

    @Column(name = "s3_key", length = 500)
    private String s3Key;

    @Column(length = 1000)
    private String url;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "rekognition_job_id", length = 200)
    private String rekognitionJobId;

    @Column(name = "moderation_reason", length = 500)
    private String moderationReason;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    private Media(Long memberSno, MediaType type, MediaStatus status, String s3Key, String url, String mimeType, Long fileSizeBytes) {
        this.memberSno = memberSno;
        this.type = type;
        this.status = status;
        this.s3Key = s3Key;
        this.url = url;
        this.mimeType = mimeType;
        this.fileSizeBytes = fileSizeBytes;
        this.createAt = LocalDateTime.now(ZoneOffset.UTC);
        this.updateAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    // 업로드형(IMAGE/VIDEO) 생성 — presigned URL 발급 시점, 아직 모더레이션 전이라 PENDING/url 없음.
    public static Media createUpload(Long memberSno, MediaType type, String s3Key, String mimeType, Long fileSizeBytes) {
        return new Media(memberSno, type, MediaStatus.PENDING, s3Key, null, mimeType, fileSizeBytes);
    }

    // 링크는 업로드/모더레이션이 필요 없어 생성 즉시 승인 상태로 만든다.
    public static Media createLink(Long memberSno, String url) {
        return new Media(memberSno, MediaType.LINK, MediaStatus.APPROVED, null, url, null, null);
    }

    public void markInProgress(String rekognitionJobId) {
        this.rekognitionJobId = rekognitionJobId;
        this.updateAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public void approve(String url) {
        this.status = MediaStatus.APPROVED;
        this.url = url;
        this.updateAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public void reject(String reason) {
        this.status = MediaStatus.REJECTED;
        this.moderationReason = reason;
        this.updateAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public void attachToPost(Long postSno) {
        this.postSno = postSno;
        this.updateAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    public void preUpdate() {
        this.updateAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
