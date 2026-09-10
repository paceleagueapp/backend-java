package com.paceleague.record.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.paceleague.member.application.port.in.shared.GetMemberNicknamePort;
import com.paceleague.record.application.dto.RecordGpsTrackResponse;
import com.paceleague.record.application.dto.RecordListItemResponse;
import com.paceleague.record.application.port.out.RecordRepositoryPort;
import com.paceleague.record.application.port.out.RecordTrackRepositoryPort;
import com.paceleague.record.domain.entity.Record;
import com.paceleague.record.domain.entity.RecordTrack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecordQueryServiceTest {

    @Mock RecordRepositoryPort recordRepositoryPort;
    @Mock RecordTrackRepositoryPort recordTrackRepositoryPort;
    @Mock GetMemberNicknamePort getMemberNicknamePort;

    RecordQueryService service() {
        return new RecordQueryService(recordRepositoryPort, recordTrackRepositoryPort, getMemberNicknamePort,
                new ObjectMapper().registerModule(new JavaTimeModule()));
    }

    private Record record(long sno, long uno) {
        Record r = Record.create(uno, 1L, BigDecimal.valueOf(5000), LocalDateTime.now().minusHours(1),
                LocalDateTime.now(), "+09:00");
        ReflectionTestUtils.setField(r, "sno", sno);
        return r;
    }

    private RecordTrack track(long sno, long recordSno, String pointsJson, int pointCount) {
        RecordTrack t = RecordTrack.builder()
                .uno(7L).clientRunId("run-" + sno).activityType("RUNNING").territoryMode(true)
                .schemaVersion(1).utcOffset("+09:00").startedAt(LocalDateTime.now().minusHours(1))
                .build();
        ReflectionTestUtils.setField(t, "sno", sno);
        ReflectionTestUtils.setField(t, "recordSno", recordSno);
        ReflectionTestUtils.setField(t, "status", RecordTrack.STATUS_FINISHED);
        ReflectionTestUtils.setField(t, "pointsJson", pointsJson);
        ReflectionTestUtils.setField(t, "pointCount", pointCount);
        ReflectionTestUtils.setField(t, "distanceMeters", BigDecimal.valueOf(4990));
        ReflectionTestUtils.setField(t, "endedAt", LocalDateTime.now());
        return t;
    }

    @Test
    void 내_러닝_목록은_GPS_트랙_보유여부를_채운다() {
        when(recordRepositoryPort.findByUnoOrderByStartTimeDesc(7L))
                .thenReturn(List.of(record(10L, 7L), record(11L, 7L)));
        when(recordTrackRepositoryPort.findRecordSnosByUno(7L)).thenReturn(List.of(10L));

        List<RecordListItemResponse> result = service().listMyRecords(7L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).recordSno()).isEqualTo(10L);
        assertThat(result.get(0).hasGpsTrack()).isTrue();
        assertThat(result.get(1).recordSno()).isEqualTo(11L);
        assertThat(result.get(1).hasGpsTrack()).isFalse();
    }

    @Test
    void GPS_트랙_조회는_points_json을_파싱해_좌표배열로_내려준다() {
        String json = "[{\"sequence\":1,\"recordedAt\":\"2026-09-01T00:00:00Z\",\"latitude\":37.1,\"longitude\":127.2,"
                + "\"altitudeMeters\":10.0,\"accuracyMeters\":5.0,\"rawLatitude\":37.1,\"rawLongitude\":127.2},"
                + "{\"sequence\":2,\"recordedAt\":\"2026-09-01T00:00:05Z\",\"latitude\":37.11,\"longitude\":127.21,"
                + "\"altitudeMeters\":11.0,\"accuracyMeters\":4.0,\"rawLatitude\":37.11,\"rawLongitude\":127.21}]";
        when(recordRepositoryPort.findBySnoAndUno(100L, 7L)).thenReturn(Optional.of(record(100L, 7L)));
        when(recordTrackRepositoryPort.findByRecordSno(100L)).thenReturn(Optional.of(track(55L, 100L, json, 2)));

        RecordGpsTrackResponse res = service().getGpsTrack(7L, 100L);

        assertThat(res.recordSno()).isEqualTo(100L);
        assertThat(res.trackSno()).isEqualTo(55L);
        assertThat(res.territoryMode()).isTrue();
        assertThat(res.pointCount()).isEqualTo(2);
        assertThat(res.points()).hasSize(2);
        assertThat(res.points().get(0).latitude()).isEqualTo(37.1);
        assertThat(res.points().get(1).longitude()).isEqualTo(127.21);
    }

    @Test
    void 남의_러닝_GPS는_조회할_수_없다() {
        when(recordRepositoryPort.findBySnoAndUno(100L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().getGpsTrack(7L, 100L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void GPS없이_수동저장된_러닝은_트랙이_없어_400() {
        when(recordRepositoryPort.findBySnoAndUno(100L, 7L)).thenReturn(Optional.of(record(100L, 7L)));
        when(recordTrackRepositoryPort.findByRecordSno(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().getGpsTrack(7L, 100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GPS 트랙이 없습니다");
    }

    @Test
    void 관리자_러닝데이터_목록은_소유자_닉네임을_채워_반환한다() {
        var pageable = org.springframework.data.domain.PageRequest.of(
                0, 20, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "startTime"));
        when(recordRepositoryPort.findAllForAdmin(pageable)).thenReturn(
                new org.springframework.data.domain.PageImpl<>(List.of(record(10L, 7L)), pageable, 1));
        when(getMemberNicknamePort.getNickname(7L)).thenReturn("달리는곰");

        var result = service().listRecords(0, 20);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).memberNickname()).isEqualTo("달리는곰");
        assertThat(result.getContent().get(0).recordSno()).isEqualTo(10L);
    }
}
