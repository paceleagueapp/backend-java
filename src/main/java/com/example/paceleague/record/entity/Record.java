package com.example.paceleague.record.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "record")
public class Record {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sno")
    private Long sno;

    @Column(name = "uno", nullable = false)
    private Long uno;

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

    protected Record() {}

    public Record(Long uno, BigDecimal distanceRecord, LocalDateTime startTime, LocalDateTime endTime) {
        this.uno = uno;
        this.distanceRecord = distanceRecord;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createAt = LocalDateTime.now();
        this.updateAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updateAt = LocalDateTime.now();
    }

    public Long getSno() { return sno; }
    public Long getUno() { return uno; }
    public BigDecimal getDistanceRecord() { return distanceRecord; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public LocalDateTime getCreateAt() { return createAt; }
    public LocalDateTime getUpdateAt() { return updateAt; }
}
