package com.example.paceleague.record.service;

import com.example.paceleague.rank.entity.Rank;
import com.example.paceleague.rank.repository.RankRepository;
import com.example.paceleague.record.dto.RecordCreateRequest;
import com.example.paceleague.record.repository.RecordRepository;
import com.example.paceleague.season.entity.Season;
import com.example.paceleague.season.repository.SeasonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.paceleague.record.entity.Record;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RecordServiceImpl implements RecordService{
    private final RecordRepository recordRepository;
    private final SeasonRepository seasonRepository;
    private final RankRepository rankRepository;

    public RecordServiceImpl(RecordRepository recordRepository, SeasonRepository seasonRepository, RankRepository rankRepository) {

        this.recordRepository = recordRepository;

        this.seasonRepository = seasonRepository;

        this.rankRepository = rankRepository;
    }

    @Transactional
    public Long create(Long uno, RecordCreateRequest req) {
        validateRequest(uno, req);

        Season seasonData = seasonRepository.findTopByOrderByStartDtDesc();

        Record record = Record.create(
                uno,
                seasonData.getSeason(),
                req.distanceRecord(),
                req.startTime(),
                req.endTime(),
                req.utcOffset()
        );

        Record savedRecord = recordRepository.save(record);

        saveRank(uno, req);

        return savedRecord.getSno();
    }

    @Transactional
    public List<Long> createBulk(Long uno, List<RecordCreateRequest> reqList) {
        if (uno == null || uno <= 0) {
            throw new IllegalArgumentException("uno is invalid");
        }
        if (reqList == null || reqList.isEmpty()) {
            throw new IllegalArgumentException("records is empty");
        }
        if (reqList.size() > 200) {
            throw new IllegalArgumentException("too many records (max 200)");
        }

        Season seasonData = seasonRepository.findTopByOrderByStartDtDesc();

        List<Long> savedIds = reqList.stream().map(req -> {
            validateRequest(uno, req);

            Record record = Record.create(
                    uno,
                    seasonData.getSeason(),
                    req.distanceRecord(),
                    req.startTime(),
                    req.endTime(),
                    req.utcOffset()
            );

            Record savedRecord = recordRepository.save(record);

            saveRank(uno, req);

            return savedRecord.getSno();
        }).toList();

        return savedIds;
    }

    private void validateRequest(Long uno, RecordCreateRequest req) {
        if (uno == null || uno <= 0) {
            throw new IllegalArgumentException("uno is invalid");
        }
        if (req.distanceRecord() == null) {
            throw new IllegalArgumentException("distanceRecord is required");
        }
        if (req.startTime() == null) {
            throw new IllegalArgumentException("startTime is required");
        }
        if (req.endTime() == null) {
            throw new IllegalArgumentException("endTime is required");
        }
        if (req.endTime().isBefore(req.startTime())) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
    }

    private void saveRank(Long uno, RecordCreateRequest req) {
        long durationSeconds = Duration.between(req.startTime(), req.endTime()).getSeconds();

        BigDecimal distanceKm = req.distanceRecord()
                .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);

        // 기본 점수 = 거리(km) * 10
        int baseScore = distanceKm.multiply(BigDecimal.TEN)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        // pace 계산
        long paceSecondsPerKm = BigDecimal.valueOf(durationSeconds)
                .divide(distanceKm, 0, RoundingMode.HALF_UP)
                .longValue();

        int scaledScore = 0;

        // 5:30/km 이하 -> 20%
        if (paceSecondsPerKm <= 330) {
            scaledScore = BigDecimal.valueOf(baseScore)
                    .multiply(BigDecimal.valueOf(0.2))
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValue();
        }
        // 6:30/km 이하 -> 10%
        else if (paceSecondsPerKm <= 390) {
            scaledScore = BigDecimal.valueOf(baseScore)
                    .multiply(BigDecimal.valueOf(0.1))
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValue();
        }

        // 주간 횟수 계산
        LocalDateTime weekStart = req.startTime()
                .with(DayOfWeek.MONDAY)
                .toLocalDate()
                .atStartOfDay();

        LocalDateTime weekEnd = weekStart.plusDays(7);

        long weeklyRunCount = recordRepository
                .countByUnoAndStartTimeGreaterThanEqualAndStartTimeLessThan(uno, weekStart, weekEnd);

        int addScore = 0;
        if (weeklyRunCount >= 5) {
            addScore = 120;
        } else if (weeklyRunCount >= 3) {
            addScore = 50;
        }

        int totalScore = baseScore + scaledScore + addScore;

        Rank rank = Rank.create(
                uno,
                totalScore,
                scaledScore,
                addScore,
                req.utcOffset()
        );

        rankRepository.save(rank);
    }
}
