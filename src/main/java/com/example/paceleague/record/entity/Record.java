package com.example.paceleague.record.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Record {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sno")
    private Long sno;

    @Column(name = "uno", nullable = false)
    private Long uno;

    @Column(name = "season", nullable = false)
    private Long season;

    @Column(name = "distance_record")
    private BigDecimal distanceRecord;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "utc_offset")
    private String utcOffset;

    public Record(Long uno, Long season, BigDecimal distanceRecord, LocalDateTime startTime, LocalDateTime endTime, String utcOffset) {
        this.uno = uno;
        this.season = season;
        this.distanceRecord = distanceRecord;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createAt = LocalDateTime.now();
        this.updateAt = LocalDateTime.now();
        this.utcOffset = utcOffset;
    }

    public static Record create(Long uno, Long season, BigDecimal distanceRecord, LocalDateTime startTime, LocalDateTime endTime, String utcOffset) {
        return new Record(
                uno,
                season,
                distanceRecord,
                startTime,
                endTime,
                utcOffset
        );
    }

    @PreUpdate
    public void preUpdate() {
        this.updateAt = LocalDateTime.now();
    }
}
