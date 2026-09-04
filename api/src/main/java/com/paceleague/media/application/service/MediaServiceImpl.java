package com.paceleague.media.application.service;

import com.paceleague.media.application.dto.MediaStatusResponse;
import com.paceleague.media.application.dto.MediaUploadInitResponse;
import com.paceleague.media.application.dto.MediaUploadRequest;
import com.paceleague.media.application.port.in.MediaService;
import com.paceleague.media.application.port.out.MediaRepositoryPort;
import com.paceleague.media.domain.entity.Media;
import com.paceleague.media.domain.enums.MediaStatus;
import com.paceleague.media.domain.enums.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.ContentModerationDetection;
import software.amazon.awssdk.services.rekognition.model.DetectModerationLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectModerationLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.GetContentModerationRequest;
import software.amazon.awssdk.services.rekognition.model.GetContentModerationResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.ModerationLabel;
import software.amazon.awssdk.services.rekognition.model.S3Object;
import software.amazon.awssdk.services.rekognition.model.StartContentModerationRequest;
import software.amazon.awssdk.services.rekognition.model.StartContentModerationResponse;
import software.amazon.awssdk.services.rekognition.model.Video;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MediaServiceImpl implements MediaService {

    private static final Map<String, String> ALLOWED_IMAGE_TYPES = Map.of(
            "image/jpeg", "jpg", "image/png", "png", "image/webp", "webp", "image/gif", "gif");
    private static final Map<String, String> ALLOWED_VIDEO_TYPES = Map.of(
            "video/mp4", "mp4", "video/quicktime", "mov");

    private static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024;
    private static final long MAX_VIDEO_SIZE_BYTES = 200L * 1024 * 1024;
    private static final Duration PRESIGN_EXPIRY = Duration.ofMinutes(5);
    // Rekognition의 라벨 신뢰도(0~100) 임계값 — 이 이상이면 유해 콘텐츠로 판단해 거부한다.
    private static final float MODERATION_MIN_CONFIDENCE = 60f;
    private static final int MODERATION_REASON_MAX_LENGTH = 500;

    @Value("${app.media.bucket}")
    private String bucket;

    private final MediaRepositoryPort mediaRepositoryPort;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final RekognitionClient rekognitionClient;

    @Transactional
    public MediaUploadInitResponse requestUpload(Long memberSno, MediaUploadRequest req) {
        requireUploadableType(req.type());
        String extension = requireAllowedMimeType(req.type(), req.mimeType());
        requireWithinDeclaredSize(req.type(), req.fileSizeBytes());

        String key = buildObjectKey(memberSno, req.type(), extension);
        Media media = Media.createUpload(memberSno, req.type(), key, req.mimeType(), req.fileSizeBytes());
        mediaRepositoryPort.save(media);

        String uploadUrl = presignPutUrl(key, req.mimeType());
        return new MediaUploadInitResponse(media.getSno(), uploadUrl, PRESIGN_EXPIRY.toSeconds());
    }

    @Transactional
    public MediaStatusResponse completeUpload(Long memberSno, Long mediaSno) {
        Media media = mediaRepositoryPort.findBySnoAndMemberSno(mediaSno, memberSno)
                .orElseThrow(() -> new IllegalArgumentException("media not found"));

        if (media.getType() == MediaType.LINK) {
            throw new IllegalArgumentException("link media does not need upload completion");
        }
        // 이미 처리된(APPROVED/REJECTED) 미디어의 재요청은 그대로 현재 상태만 반환 — 멱등하게 처리.
        if (media.getStatus() != MediaStatus.PENDING) {
            return MediaStatusResponse.from(media);
        }

        long actualSize = headObjectSize(media.getS3Key());
        long maxSize = media.getType() == MediaType.IMAGE ? MAX_IMAGE_SIZE_BYTES : MAX_VIDEO_SIZE_BYTES;
        if (actualSize > maxSize) {
            deleteObject(media.getS3Key());
            media.reject("file too large");
            return MediaStatusResponse.from(media);
        }

        if (media.getType() == MediaType.IMAGE) {
            moderateImageSync(media);
        } else {
            startVideoModeration(media);
        }
        return MediaStatusResponse.from(media);
    }

    @Transactional
    public MediaStatusResponse getUploadStatus(Long memberSno, Long mediaSno) {
        Media media = mediaRepositoryPort.findBySnoAndMemberSno(mediaSno, memberSno)
                .orElseThrow(() -> new IllegalArgumentException("media not found"));

        if (media.getStatus() == MediaStatus.PENDING && media.getRekognitionJobId() != null) {
            pollVideoModeration(media);
        }
        return MediaStatusResponse.from(media);
    }

    @Transactional
    public Long createLink(Long memberSno, String url) {
        requireHttpUrl(url);
        Media media = Media.createLink(memberSno, url.trim());
        mediaRepositoryPort.save(media);
        return media.getSno();
    }

    @Transactional
    public void attachToPost(Long memberSno, List<Long> mediaSnos, Long postSno) {
        for (Long mediaSno : mediaSnos) {
            Media media = mediaRepositoryPort.findBySnoAndMemberSno(mediaSno, memberSno)
                    .orElseThrow(() -> new IllegalArgumentException("media not found"));
            if (media.getStatus() != MediaStatus.APPROVED) {
                throw new IllegalArgumentException("media is not approved");
            }
            if (media.getPostSno() != null) {
                throw new IllegalArgumentException("media is already attached to a post");
            }
            media.attachToPost(postSno);
        }
    }

    private void moderateImageSync(Media media) {
        DetectModerationLabelsResponse response = rekognitionClient.detectModerationLabels(
                DetectModerationLabelsRequest.builder()
                        .image(Image.builder().s3Object(s3ObjectRef(media.getS3Key())).build())
                        .minConfidence(MODERATION_MIN_CONFIDENCE)
                        .build());

        if (response.hasModerationLabels() && !response.moderationLabels().isEmpty()) {
            deleteObject(media.getS3Key());
            media.reject(joinImageLabels(response.moderationLabels()));
        } else {
            media.approve(buildPublicUrl(media.getS3Key()));
        }
    }

    private void startVideoModeration(Media media) {
        StartContentModerationResponse response = rekognitionClient.startContentModeration(
                StartContentModerationRequest.builder()
                        .video(Video.builder().s3Object(s3ObjectRef(media.getS3Key())).build())
                        .minConfidence(MODERATION_MIN_CONFIDENCE)
                        .build());
        media.markInProgress(response.jobId());
    }

    private void pollVideoModeration(Media media) {
        GetContentModerationResponse response = rekognitionClient.getContentModeration(
                GetContentModerationRequest.builder().jobId(media.getRekognitionJobId()).build());

        switch (response.jobStatus()) {
            case SUCCEEDED -> {
                if (response.hasModerationLabels() && !response.moderationLabels().isEmpty()) {
                    deleteObject(media.getS3Key());
                    media.reject(joinVideoLabels(response.moderationLabels()));
                } else {
                    media.approve(buildPublicUrl(media.getS3Key()));
                }
            }
            case FAILED -> media.reject("moderation failed");
            default -> {
                // IN_PROGRESS(그 외 상태 포함) — 다음 폴링까지 PENDING 유지
            }
        }
    }

    private S3Object s3ObjectRef(String key) {
        return S3Object.builder().bucket(bucket).name(key).build();
    }

    private String buildObjectKey(Long memberSno, MediaType type, String extension) {
        return "media/%s/%d/%s.%s".formatted(type.name().toLowerCase(), memberSno, UUID.randomUUID(), extension);
    }

    private String presignPutUrl(String key, String mimeType) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket).key(key).contentType(mimeType).build();
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(PRESIGN_EXPIRY)
                .putObjectRequest(objectRequest)
                .build();
        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }

    private long headObjectSize(String key) {
        try {
            return s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build()).contentLength();
        } catch (NoSuchKeyException e) {
            throw new IllegalArgumentException("upload not found — PUT the file to the presigned URL before completing");
        }
    }

    private void deleteObject(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    }

    private String buildPublicUrl(String key) {
        return "https://%s.s3.%s.amazonaws.com/%s".formatted(bucket, Region.AP_NORTHEAST_2.id(), key);
    }

    private String joinImageLabels(List<ModerationLabel> labels) {
        return truncate(labels.stream().map(ModerationLabel::name).distinct().collect(Collectors.joining(", ")));
    }

    private String joinVideoLabels(List<ContentModerationDetection> detections) {
        return truncate(detections.stream()
                .map(d -> d.moderationLabel().name())
                .distinct()
                .collect(Collectors.joining(", ")));
    }

    private String truncate(String reason) {
        return reason.length() > MODERATION_REASON_MAX_LENGTH ? reason.substring(0, MODERATION_REASON_MAX_LENGTH) : reason;
    }

    private void requireUploadableType(MediaType type) {
        if (type != MediaType.IMAGE && type != MediaType.VIDEO) {
            throw new IllegalArgumentException("type must be IMAGE or VIDEO");
        }
    }

    private String requireAllowedMimeType(MediaType type, String mimeType) {
        Map<String, String> allowed = type == MediaType.IMAGE ? ALLOWED_IMAGE_TYPES : ALLOWED_VIDEO_TYPES;
        String extension = allowed.get(mimeType);
        if (extension == null) {
            throw new IllegalArgumentException("unsupported mime type: " + mimeType);
        }
        return extension;
    }

    private void requireWithinDeclaredSize(MediaType type, Long fileSizeBytes) {
        if (fileSizeBytes == null || fileSizeBytes <= 0) {
            throw new IllegalArgumentException("fileSizeBytes is required");
        }
        long max = type == MediaType.IMAGE ? MAX_IMAGE_SIZE_BYTES : MAX_VIDEO_SIZE_BYTES;
        if (fileSizeBytes > max) {
            throw new IllegalArgumentException("file too large");
        }
    }

    private void requireHttpUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("url is required");
        }
        String lower = url.trim().toLowerCase();
        if (!(lower.startsWith("http://") || lower.startsWith("https://"))) {
            throw new IllegalArgumentException("only http(s) urls are allowed");
        }
        if (url.length() > 1000) {
            throw new IllegalArgumentException("url is too long");
        }
    }
}
