package com.paceleague.territory.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 땅따먹기의 "땅" 1구획. 러닝 GPS 경로가 이룬 닫힌 도형 하나가 territory 한 행이 된다.
//  - polygon_json: 소유 헥사곤 합집합 외곽선 위/경도 링([[lat,lng], ...]). 지도에 그대로 그린다.
//  - bbox_*: 지도 bounds 조회용 경계 상자(공간 인덱스 대신 DECIMAL 범위 비교).
//  - HP 없음(2026-09-05 제거) — 겹치는 러닝이 있으면 즉시 소유가 바뀐다.
//  - 2026-09-07: 겹침 판정이 헥사곤 단위로 바뀌면서 "점령"도 땅 전체가 아니라 겹친 헥사곤만 옮긴다.
//    그래서 이 행의 소유자(owner_member_sno)는 이제 절대 바뀌지 않는다 — 남의 헥사곤을 뺏으면 그 헥사곤들은
//    뺏은 사람의 새 territory 행으로 편입되고, 뺏긴 땅은 남은 헥사곤만으로 도형이 줄어든다(recomputeFromHexes).
//    남은 헥사곤이 하나도 없으면 이 행 자체가 삭제된다. (자세한 흐름은 ProcessTerritoryRunService 참고)
@Entity
@Table(name = "territory")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Territory {

    public static final String STATUS_ACTIVE = "ACTIVE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sno")
    private Long sno;

    @Column(name = "owner_member_sno", nullable = false)
    private Long ownerMemberSno;

    // 생성 시점 시즌 번호 스냅샷(record.season과 동일 규칙). 시즌 리셋은 이후 단계.
    @Column(name = "season")
    private Long season;

    @Column(name = "polygon_json", columnDefinition = "LONGTEXT")
    private String polygonJson;

    @Column(name = "bbox_min_lat", precision = 10, scale = 7)
    private BigDecimal bboxMinLat;

    @Column(name = "bbox_min_lng", precision = 10, scale = 7)
    private BigDecimal bboxMinLng;

    @Column(name = "bbox_max_lat", precision = 10, scale = 7)
    private BigDecimal bboxMaxLat;

    @Column(name = "bbox_max_lng", precision = 10, scale = 7)
    private BigDecimal bboxMaxLng;

    @Column(name = "center_lat", precision = 10, scale = 7)
    private BigDecimal centerLat;

    @Column(name = "center_lng", precision = 10, scale = 7)
    private BigDecimal centerLng;

    @Column(name = "area_sqm", precision = 18, scale = 4)
    private BigDecimal areaSqm;

    @Column(name = "perimeter_m", precision = 14, scale = 4)
    private BigDecimal perimeterM;

    // 이 땅을 이루는 H3(resolution 12) 헥사곤 개수. 생성 시 정해지지만, 이후 남의 러닝이 이 헥사곤 중
    // 일부만 겹쳐 뺏어가면 recomputeFromHexes로 줄어들 수 있다(부분 점령, 2026-09-07).
    @Column(name = "hex_count")
    private Integer hexCount;

    @Column(name = "source_record_sno")
    private Long sourceRecordSno;

    @Column(name = "source_track_sno")
    private Long sourceTrackSno;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Builder
    private Territory(Long ownerMemberSno, Long season, String polygonJson,
                      BigDecimal bboxMinLat, BigDecimal bboxMinLng, BigDecimal bboxMaxLat, BigDecimal bboxMaxLng,
                      BigDecimal centerLat, BigDecimal centerLng, BigDecimal areaSqm, BigDecimal perimeterM,
                      Integer hexCount, Long sourceRecordSno, Long sourceTrackSno) {
        this.ownerMemberSno = ownerMemberSno;
        this.season = season;
        this.polygonJson = polygonJson;
        this.bboxMinLat = bboxMinLat;
        this.bboxMinLng = bboxMinLng;
        this.bboxMaxLat = bboxMaxLat;
        this.bboxMaxLng = bboxMaxLng;
        this.centerLat = centerLat;
        this.centerLng = centerLng;
        this.areaSqm = areaSqm;
        this.perimeterM = perimeterM;
        this.hexCount = hexCount;
        this.sourceRecordSno = sourceRecordSno;
        this.sourceTrackSno = sourceTrackSno;
        this.status = STATUS_ACTIVE;
        this.createAt = LocalDateTime.now();
        this.updateAt = this.createAt;
    }

    public boolean isOwnedBy(Long memberSno) {
        return memberSno != null && memberSno.equals(this.ownerMemberSno);
    }

    // 이 땅의 헥사곤 집합이 바뀌었을 때(도형·면적·hexCount·bbox) 갱신한다. 소유자/시즌/생성 출처는 그대로 둔다.
    // 두 호출부가 쓴다: TerritoryHexBackfillService(H3 도입 전 땅을 헥사곤 집합으로 처음 환산할 때)와
    // ProcessTerritoryRunService(다른 러닝이 이 땅의 헥사곤 일부만 뺏어가 남은 헥사곤으로 도형이 줄어들 때).
    public void recomputeFromHexes(int hexCount, double areaSqm, String polygonJson,
                                  double bboxMinLat, double bboxMinLng, double bboxMaxLat, double bboxMaxLng) {
        this.hexCount = hexCount;
        this.areaSqm = BigDecimal.valueOf(areaSqm);
        this.polygonJson = polygonJson;
        this.bboxMinLat = BigDecimal.valueOf(bboxMinLat);
        this.bboxMinLng = BigDecimal.valueOf(bboxMinLng);
        this.bboxMaxLat = BigDecimal.valueOf(bboxMaxLat);
        this.bboxMaxLng = BigDecimal.valueOf(bboxMaxLng);
        this.updateAt = LocalDateTime.now();
    }
}
