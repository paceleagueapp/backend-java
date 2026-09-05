package com.paceleague.territory.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 땅따먹기의 "땅" 1구획. 러닝 GPS 경로가 이룬 닫힌 도형 하나가 territory 한 행이 된다.
//  - polygon_json: 실제 러닝 경로 기반 위/경도 링([[lat,lng], ...]). 지도에 그대로 그린다.
//  - bbox_*: 지도 bounds 조회용 경계 상자(공간 인덱스 대신 DECIMAL 범위 비교).
//  - hp/max_hp: 겹치는 러닝에 데미지를 입고, 0이 되면 최다 기여자에게 소유권이 넘어간다.
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

    // 이 땅을 이루는 H3(resolution 12) 헥사곤 개수. 생성 시 고정되며 점령(capture)으로도 바뀌지 않는다 —
    // territory_hex 행 자체는 소유자 변경과 무관하게 그대로 이 territory_sno에 남는다.
    @Column(name = "hex_count")
    private Integer hexCount;

    @Column(name = "hp", nullable = false)
    private int hp;

    @Column(name = "max_hp", nullable = false)
    private int maxHp;

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
                      Integer hexCount, int maxHp, Long sourceRecordSno, Long sourceTrackSno) {
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
        this.hp = maxHp;
        this.maxHp = maxHp;
        this.sourceRecordSno = sourceRecordSno;
        this.sourceTrackSno = sourceTrackSno;
        this.status = STATUS_ACTIVE;
        this.createAt = LocalDateTime.now();
        this.updateAt = this.createAt;
    }

    // 남의 러닝이 이 땅을 겹쳐 지나가 데미지를 입는다.
    public void applyDamage(int amount) {
        this.hp -= amount;
        this.updateAt = LocalDateTime.now();
    }

    // 소유자가 자기 땅을 다시 달려 체력을 회복한다(최대치 초과 불가).
    public void heal(int amount) {
        this.hp = Math.min(this.maxHp, this.hp + amount);
        this.updateAt = LocalDateTime.now();
    }

    // HP 0 → 최다 기여자에게 즉시 소유권 이전 + HP 리셋.
    public void capture(Long newOwnerMemberSno) {
        this.ownerMemberSno = newOwnerMemberSno;
        this.hp = this.maxHp;
        this.updateAt = LocalDateTime.now();
    }

    public boolean isDepleted() {
        return this.hp <= 0;
    }

    public boolean isOwnedBy(Long memberSno) {
        return memberSno != null && memberSno.equals(this.ownerMemberSno);
    }
}
