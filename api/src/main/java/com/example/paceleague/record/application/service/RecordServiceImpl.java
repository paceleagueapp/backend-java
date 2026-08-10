package com.example.paceleague.record.application.service;

import com.example.paceleague.rank.application.dto.ApplyScoreCommand;
import com.example.paceleague.rank.application.port.in.ApplyScoreUseCase;
import com.example.paceleague.record.application.dto.RecordCreateRequest;
import com.example.paceleague.record.application.port.in.RecordService;
import com.example.paceleague.record.application.port.out.RecordRepositoryPort;
import com.example.paceleague.record.domain.entity.Record;
import com.example.paceleague.record.domain.policy.RecordScoreCalculator;
import com.example.paceleague.season.application.port.in.GetCurrentSeasonPort;
import com.example.paceleague.season.domain.entity.Season;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RecordServiceImpl implements RecordService {
    // 울트라마라톤급 장거리까지 포괄하는 보수적 상한 — 이보다 크면 조작된 값으로 간주.
    private static final BigDecimal MAX_DISTANCE_METERS = BigDecimal.valueOf(300_000);
    // 1km당 100초(시속 36km) 미만은 어떤 실제 러닝 기록으로도 나올 수 없는 페이스.
    private static final long MIN_PACE_SECONDS_PER_KM = 100;

    private final RecordRepositoryPort recordRepositoryPort;
    private final GetCurrentSeasonPort getCurrentSeasonPort;
    private final ApplyScoreUseCase applyScoreUseCase;

    public RecordServiceImpl(RecordRepositoryPort recordRepositoryPort, GetCurrentSeasonPort getCurrentSeasonPort, ApplyScoreUseCase applyScoreUseCase) {
        this.recordRepositoryPort = recordRepositoryPort;
        this.getCurrentSeasonPort = getCurrentSeasonPort;
        this.applyScoreUseCase = applyScoreUseCase;
    }

    @Transactional
    public Long create(Long uno, RecordCreateRequest req) {
        validateRequest(uno, req);

        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusMonths(1);

        boolean duplicated = recordRepositoryPort
                .findByUnoAndStartTimeBetween(uno, from, to)
                .stream()
                .anyMatch(record -> record.getStartTime().equals(req.startTime()));

        if (duplicated) {
            throw new IllegalArgumentException("duplicate record");
        }

        Season seasonData = getCurrentSeasonPort.getCurrentSeason();

        return saveRecordAndRank(uno, seasonData.getSeason(), req);
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

        reqList.forEach(req -> validateRequest(uno, req));

        Season seasonData = getCurrentSeasonPort.getCurrentSeason();

        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusMonths(1);

        Set<LocalDateTime> existingStartTimes = recordRepositoryPort
                .findByUnoAndStartTimeBetween(uno, from, to)
                .stream()
                .map(Record::getStartTime)
                .collect(Collectors.toSet());

        List<Long> savedIds = new ArrayList<>();

        for (RecordCreateRequest req : reqList) {
            if (existingStartTimes.contains(req.startTime())) {
                continue;
            }

            savedIds.add(saveRecordAndRank(uno, seasonData.getSeason(), req));
            existingStartTimes.add(req.startTime());
        }

        return savedIds;
    }

    private Long saveRecordAndRank(Long uno, Long seasonNo, RecordCreateRequest req) {
        Record record = Record.create(
                uno,
                seasonNo,
                req.distanceRecord(),
                req.startTime(),
                req.endTime(),
                req.utcOffset()
        );

        Record savedRecord = recordRepositoryPort.save(record);
        computeAndApplyScore(uno, seasonNo, req);

        return savedRecord.getSno();
    }

    private void validateRequest(Long uno, RecordCreateRequest req) {
        if (uno == null || uno <= 0) {
            throw new IllegalArgumentException("uno is invalid");
        }
        if (req.distanceRecord() == null) {
            throw new IllegalArgumentException("distanceRecord is required");
        }
        if (req.distanceRecord().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("distanceRecord must be positive");
        }
        if (req.distanceRecord().compareTo(MAX_DISTANCE_METERS) > 0) {
            throw new IllegalArgumentException("distanceRecord exceeds maximum allowed distance");
        }
        if (req.startTime() == null) {
            throw new IllegalArgumentException("startTime is required");
        }
        if (req.endTime() == null) {
            throw new IllegalArgumentException("endTime is required");
        }
        if (!req.endTime().isAfter(req.startTime())) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
        if (req.startTime().isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new IllegalArgumentException("startTime cannot be in the future");
        }

        BigDecimal distanceKm = req.distanceRecord().divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
        long durationSeconds = Duration.between(req.startTime(), req.endTime()).getSeconds();
        long paceSecondsPerKm = BigDecimal.valueOf(durationSeconds)
                .divide(distanceKm, 0, RoundingMode.HALF_UP)
                .longValue();
        if (paceSecondsPerKm < MIN_PACE_SECONDS_PER_KM) {
            throw new IllegalArgumentException("pace is physically implausible");
        }
    }

    private void computeAndApplyScore(Long uno, Long seasonSno, RecordCreateRequest req) {
        BigDecimal distanceKm = req.distanceRecord()
                .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
        long durationSeconds = Duration.between(req.startTime(), req.endTime()).getSeconds();

        int baseScore = RecordScoreCalculator.calculateBaseScore(distanceKm);
        int scaledScore = RecordScoreCalculator.calculatePaceBonus(baseScore, durationSeconds, distanceKm);
        int addScore = calculateWeeklyBonus(uno, req.startTime());
        int totalScore = baseScore + scaledScore + addScore;

        applyScoreUseCase.applyScore(new ApplyScoreCommand(uno, seasonSno, totalScore, scaledScore, addScore, req.utcOffset()));
    }

    // 이번 기록이 속한 주(월~일)의 기록 수가 5회 이상이면 120점, 3회 이상이면 50점
    private int calculateWeeklyBonus(Long uno, LocalDateTime startTime) {
        LocalDateTime weekStart = startTime.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
        LocalDateTime weekEnd = weekStart.plusDays(7);

        long weeklyRunCount = recordRepositoryPort
                .countByUnoAndStartTimeGreaterThanEqualAndStartTimeLessThan(uno, weekStart, weekEnd);

        if (weeklyRunCount >= 5) {
            return 120;
        } else if (weeklyRunCount >= 3) {
            return 50;
        }
        return 0;
    }
}
