package com.paceleague.coupon.application.service;

import com.paceleague.coupon.application.dto.CouponImageUploadResponse;
import com.paceleague.coupon.application.dto.CouponResponse;
import com.paceleague.coupon.application.port.in.CouponUseCase;
import com.paceleague.coupon.application.port.in.shared.AdminCouponUseCase;
import com.paceleague.coupon.application.port.in.shared.PurgeMemberCouponsPort;
import com.paceleague.coupon.application.port.out.CouponImageStoragePort;
import com.paceleague.coupon.application.port.out.CouponRepositoryPort;
import com.paceleague.coupon.domain.entity.Coupon;
import com.paceleague.member.application.dto.AdminMemberDetail;
import com.paceleague.member.application.port.in.shared.AdminMemberQueryPort;
import com.paceleague.notification.application.port.in.shared.SendMemberPushPort;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService implements CouponUseCase, AdminCouponUseCase, PurgeMemberCouponsPort {

    private static final Logger log = LoggerFactory.getLogger(CouponService.class);

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final Map<String, String> ALLOWED_IMAGE_TYPES = Map.of(
            "image/jpeg", "jpg", "image/png", "png", "image/webp", "webp");
    private static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024;
    private static final Duration UPLOAD_URL_EXPIRY = Duration.ofMinutes(5);
    private static final Duration IMAGE_URL_EXPIRY = Duration.ofMinutes(10);
    private static final Pattern IMAGE_KEY_PATTERN = Pattern.compile("^coupon/[0-9a-f-]{36}\\.(jpg|png|webp)$");
    private static final int NAME_MAX_LENGTH = 100;

    static final String PUSH_KIND = "COUPON";
    static final String PUSH_DEEPLINK = "paceleague://coupons";

    private final CouponRepositoryPort couponRepositoryPort;
    private final CouponImageStoragePort couponImageStoragePort;
    private final AdminMemberQueryPort adminMemberQueryPort;
    private final SendMemberPushPort sendMemberPushPort;

    @Override
    public List<CouponResponse> listMine(Long memberSno) {
        return toResponses(couponRepositoryPort.findAllByMemberSno(memberSno));
    }

    @Override
    @Transactional
    public CouponResponse markUsed(Long memberSno, Long couponSno) {
        Coupon coupon = couponRepositoryPort.findBySnoAndMemberSno(couponSno, memberSno)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found."));
        LocalDate today = today();
        coupon.markUsed(today);
        couponRepositoryPort.save(coupon);
        return CouponResponse.of(coupon, imageUrl(coupon), today);
    }

    @Override
    public CouponImageUploadResponse requestImageUpload(String mimeType, Long fileSizeBytes) {
        String extension = ALLOWED_IMAGE_TYPES.get(mimeType);
        if (extension == null) {
            throw new IllegalArgumentException("Only JPEG, PNG or WEBP images are allowed.");
        }
        if (fileSizeBytes == null || fileSizeBytes <= 0 || fileSizeBytes > MAX_IMAGE_SIZE_BYTES) {
            throw new IllegalArgumentException("Image must be 10MB or smaller.");
        }
        String key = "coupon/%s.%s".formatted(UUID.randomUUID(), extension);
        String url = couponImageStoragePort.presignUpload(key, mimeType, UPLOAD_URL_EXPIRY);
        return new CouponImageUploadResponse(url, key, UPLOAD_URL_EXPIRY.toSeconds());
    }

    @Override
    @Transactional
    public CouponResponse grant(Long adminSno, Long memberSno, String name, LocalDate expiresAt, String imageKey) {
        String trimmedName = name == null ? "" : name.trim();
        if (trimmedName.isEmpty() || trimmedName.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("Coupon name is required (max 100 characters).");
        }
        LocalDate today = today();
        if (expiresAt == null || expiresAt.isBefore(today)) {
            throw new IllegalArgumentException("Expiry date must be today or later.");
        }
        if (imageKey == null || !IMAGE_KEY_PATTERN.matcher(imageKey).matches()) {
            throw new IllegalArgumentException("Invalid coupon image.");
        }
        AdminMemberDetail member = adminMemberQueryPort.getDetail(memberSno);
        if (!"ACTIVE".equals(member.status())) {
            throw new IllegalArgumentException("Coupons can only be given to active members.");
        }
        if (couponRepositoryPort.existsByImageKey(imageKey)) {
            throw new IllegalArgumentException("This image is already used by another coupon.");
        }
        long size = couponImageStoragePort.sizeOf(imageKey);
        if (size < 0) {
            throw new IllegalArgumentException("Coupon image has not been uploaded.");
        }
        if (size > MAX_IMAGE_SIZE_BYTES) {
            couponImageStoragePort.delete(imageKey);
            throw new IllegalArgumentException("Image must be 10MB or smaller.");
        }

        Coupon coupon = couponRepositoryPort.save(Coupon.grant(memberSno, trimmedName, imageKey, expiresAt, adminSno));
        afterCommit(() -> notifyGranted(coupon));
        return CouponResponse.of(coupon, imageUrl(coupon), today);
    }

    @Override
    public List<CouponResponse> listByMember(Long memberSno) {
        return toResponses(couponRepositoryPort.findAllByMemberSno(memberSno));
    }

    @Override
    @Transactional
    public void delete(Long couponSno) {
        Coupon coupon = couponRepositoryPort.findBySno(couponSno)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found."));
        couponRepositoryPort.delete(coupon);
        String key = coupon.getImageKey();
        afterCommit(() -> deleteImageQuietly(key));
    }

    @Override
    @Transactional
    public void purge(Long memberSno) {
        List<String> keys = couponRepositoryPort.findAllByMemberSno(memberSno).stream()
                .map(Coupon::getImageKey)
                .toList();
        couponRepositoryPort.deleteAllByMemberSno(memberSno);
        afterCommit(() -> keys.forEach(this::deleteImageQuietly));
    }

    private void notifyGranted(Coupon coupon) {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("type", "coupon");
        data.put("couponSno", String.valueOf(coupon.getSno()));
        data.put("deeplink", PUSH_DEEPLINK);
        sendMemberPushPort.send(coupon.getMemberSno(), PUSH_KIND, "쿠폰이 도착했어요 🎁",
                "'" + coupon.getName() + "' 쿠폰이 지급되었어요. 쿠폰함에서 확인하세요.", PUSH_DEEPLINK, data);
    }

    private void deleteImageQuietly(String key) {
        try {
            couponImageStoragePort.delete(key);
        } catch (RuntimeException e) {
            log.warn("쿠폰 이미지 삭제 실패 key={}", key, e);
        }
    }

    // DB 반영이 확정된 뒤에만 외부 부수효과(푸시, S3 삭제)를 실행한다. 트랜잭션 밖(단위 테스트 등)이면 바로 실행.
    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    private List<CouponResponse> toResponses(List<Coupon> coupons) {
        LocalDate today = today();
        return coupons.stream().map(c -> CouponResponse.of(c, imageUrl(c), today)).toList();
    }

    private String imageUrl(Coupon coupon) {
        return couponImageStoragePort.presignDownload(coupon.getImageKey(), IMAGE_URL_EXPIRY);
    }

    private LocalDate today() {
        return LocalDate.now(KST);
    }
}
