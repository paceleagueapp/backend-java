package com.paceleague.coupon.application.service;

import com.paceleague.coupon.application.port.out.CouponImageStoragePort;
import com.paceleague.coupon.application.port.out.CouponRepositoryPort;
import com.paceleague.coupon.domain.entity.Coupon;
import com.paceleague.member.application.dto.AdminMemberDetail;
import com.paceleague.member.application.port.in.shared.AdminMemberQueryPort;
import com.paceleague.notification.application.port.in.shared.SendMemberPushPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CouponServiceTest {

    @Mock CouponRepositoryPort couponRepositoryPort;
    @Mock CouponImageStoragePort couponImageStoragePort;
    @Mock AdminMemberQueryPort adminMemberQueryPort;
    @Mock SendMemberPushPort sendMemberPushPort;

    CouponService service;

    static final String KEY = "coupon/123e4567-e89b-12d3-a456-426614174000.png";
    static final LocalDate TODAY = LocalDate.now(ZoneId.of("Asia/Seoul"));

    @BeforeEach
    void setUp() {
        service = new CouponService(couponRepositoryPort, couponImageStoragePort, adminMemberQueryPort, sendMemberPushPort);
        when(couponImageStoragePort.presignDownload(anyString(), any())).thenReturn("https://signed");
        when(couponRepositoryPort.save(any())).thenAnswer(inv -> {
            Coupon c = inv.getArgument(0);
            ReflectionTestUtils.setField(c, "sno", 1L);
            return c;
        });
    }

    private void member(Long sno, String status) {
        when(adminMemberQueryPort.getDetail(sno)).thenReturn(
                new AdminMemberDetail(sno, "id", "닉", null, status, null, null, true, true, List.of()));
    }

    @Test
    void 업로드된_이미지로_활성_회원에게_지급하고_푸시를_보낸다() {
        member(7L, "ACTIVE");
        when(couponImageStoragePort.sizeOf(KEY)).thenReturn(1000L);

        var res = service.grant(1L, 7L, " 아메리카노 ", TODAY.plusDays(30), KEY);

        assertThat(res.name()).isEqualTo("아메리카노");
        assertThat(res.status()).isEqualTo(Coupon.STATUS_ISSUED);
        assertThat(res.imageUrl()).isEqualTo("https://signed");
        verify(sendMemberPushPort).send(eq(7L), eq(CouponService.PUSH_KIND), anyString(), anyString(),
                eq(CouponService.PUSH_DEEPLINK), any());
    }

    @Test
    void 탈퇴_회원에게는_지급할_수_없다() {
        member(7L, "WITHDRAWN");
        when(couponImageStoragePort.sizeOf(KEY)).thenReturn(1000L);

        assertThatThrownBy(() -> service.grant(1L, 7L, "커피", TODAY.plusDays(1), KEY))
                .isInstanceOf(IllegalArgumentException.class);
        verify(couponRepositoryPort, never()).save(any());
    }

    @Test
    void 업로드되지_않은_이미지면_지급_실패() {
        member(7L, "ACTIVE");
        when(couponImageStoragePort.sizeOf(KEY)).thenReturn(-1L);

        assertThatThrownBy(() -> service.grant(1L, 7L, "커피", TODAY.plusDays(1), KEY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 쿠폰_경로가_아닌_이미지키는_거부한다() {
        member(7L, "ACTIVE");

        assertThatThrownBy(() -> service.grant(1L, 7L, "커피", TODAY.plusDays(1), "media/image/0/x.png"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 지난_날짜로는_지급할_수_없다() {
        member(7L, "ACTIVE");

        assertThatThrownBy(() -> service.grant(1L, 7L, "커피", TODAY.minusDays(1), KEY))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 허용되지_않은_이미지_형식은_업로드_URL을_주지_않는다() {
        assertThatThrownBy(() -> service.requestImageUpload("image/gif", 100L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 업로드_URL은_coupon_경로의_키를_준다() {
        when(couponImageStoragePort.presignUpload(anyString(), eq("image/png"), any())).thenReturn("https://put");

        var res = service.requestImageUpload("image/png", 100L);

        assertThat(res.imageKey()).matches("^coupon/[0-9a-f-]{36}\\.png$");
        assertThat(res.uploadUrl()).isEqualTo("https://put");
    }

    @Test
    void 본인_쿠폰을_사용완료로_표시한다() {
        Coupon c = Coupon.grant(7L, "커피", KEY, TODAY, 1L);
        when(couponRepositoryPort.findBySnoAndMemberSno(1L, 7L)).thenReturn(Optional.of(c));

        var res = service.markUsed(7L, 1L);

        assertThat(res.status()).isEqualTo(Coupon.STATUS_USED);
        assertThat(res.usedAt()).isNotNull();
    }

    @Test
    void 이미_사용한_쿠폰은_다시_사용할_수_없다() {
        Coupon c = Coupon.grant(7L, "커피", KEY, TODAY, 1L);
        c.markUsed(TODAY);
        when(couponRepositoryPort.findBySnoAndMemberSno(1L, 7L)).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> service.markUsed(7L, 1L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 만료된_쿠폰은_사용할_수_없다() {
        Coupon c = Coupon.grant(7L, "커피", KEY, TODAY.minusDays(1), 1L);
        when(couponRepositoryPort.findBySnoAndMemberSno(1L, 7L)).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> service.markUsed(7L, 1L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 남의_쿠폰은_사용할_수_없다() {
        when(couponRepositoryPort.findBySnoAndMemberSno(1L, 8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markUsed(8L, 1L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 탈퇴_시_쿠폰행과_이미지를_지운다() {
        when(couponRepositoryPort.findAllByMemberSno(7L)).thenReturn(List.of(Coupon.grant(7L, "커피", KEY, TODAY, 1L)));

        service.purge(7L);

        verify(couponRepositoryPort).deleteAllByMemberSno(7L);
        verify(couponImageStoragePort).delete(KEY);
    }
}
