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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.paceleague.rank.entity.MemberScore;
import com.example.paceleague.rank.repository.MemberScoreRepository;

@Service
@Transactional(readOnly = true)
public class RecordServiceImpl implements RecordService{
    private final RecordRepository recordRepository;
    private final SeasonRepository seasonRepository;
    private final RankRepository rankRepository;
    private final MemberScoreRepository memberScoreRepository;

    public RecordServiceImpl(RecordRepository recordRepository, SeasonRepository seasonRepository, RankRepository rankRepository, MemberScoreRepository memberScoreRepository) {
        this.recordRepository = recordRepository;
        this.seasonRepository = seasonRepository;
        this.rankRepository = rankRepository;
        this.memberScoreRepository = memberScoreRepository;
    }

    @Transactional
    public Long create(Long uno, RecordCreateRequest req) {
        validateRequest(uno, req);

        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusMonths(1);

        boolean duplicated = recordRepository
                .findByUnoAndStartTimeBetween(uno, from, to)
                .stream()
                .anyMatch(record -> record.getStartTime().equals(req.startTime()));

        if (duplicated) {
            throw new IllegalArgumentException("duplicate record");
        }

        Season seasonData = seasonRepository.findTopByOrderByStartDtDesc();

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

        Season seasonData = seasonRepository.findTopByOrderByStartDtDesc();

        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusMonths(1);

        Set<LocalDateTime> existingStartTimes = recordRepository
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

        Record savedRecord = recordRepository.save(record);
        saveRank(uno, seasonNo, req);

        return savedRecord.getSno();
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

    private void saveRank(Long uno, Long seasonSno, RecordCreateRequest req) {
        BigDecimal distanceKm = req.distanceRecord()
                .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
        long durationSeconds = Duration.between(req.startTime(), req.endTime()).getSeconds();

        int baseScore = calculateBaseScore(distanceKm);
        int scaledScore = calculatePaceBonus(baseScore, durationSeconds, distanceKm);
        int addScore = calculateWeeklyBonus(uno, req.startTime());
        int totalScore = baseScore + scaledScore + addScore;

        Rank rank = Rank.create(uno, totalScore, scaledScore, addScore, req.utcOffset());
        rankRepository.save(rank);

        applyScoreToSeason(uno, seasonSno, totalScore);
    }

    // 기본 점수 = 거리(km) * 10
    private int calculateBaseScore(BigDecimal distanceKm) {
        return distanceKm.multiply(BigDecimal.TEN)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    // pace 5:30/km 이하 -> baseScore의 20%, 6:30/km 이하 -> 10%, 그 외 0
    private int calculatePaceBonus(int baseScore, long durationSeconds, BigDecimal distanceKm) {
        long paceSecondsPerKm = BigDecimal.valueOf(durationSeconds)
                .divide(distanceKm, 0, RoundingMode.HALF_UP)
                .longValue();

        BigDecimal bonusRate;
        if (paceSecondsPerKm <= 330) {
            bonusRate = BigDecimal.valueOf(0.2);
        } else if (paceSecondsPerKm <= 390) {
            bonusRate = BigDecimal.valueOf(0.1);
        } else {
            return 0;
        }

        return BigDecimal.valueOf(baseScore)
                .multiply(bonusRate)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    // 이번 기록이 속한 주(월~일)의 기록 수가 5회 이상이면 120점, 3회 이상이면 50점
    private int calculateWeeklyBonus(Long uno, LocalDateTime startTime) {
        LocalDateTime weekStart = startTime.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
        LocalDateTime weekEnd = weekStart.plusDays(7);

        long weeklyRunCount = recordRepository
                .countByUnoAndStartTimeGreaterThanEqualAndStartTimeLessThan(uno, weekStart, weekEnd);

        if (weeklyRunCount >= 5) {
            return 120;
        } else if (weeklyRunCount >= 3) {
            return 50;
        }
        return 0;
    }

    private void applyScoreToSeason(Long uno, Long seasonSno, int totalScore) {
        MemberScore memberScore = memberScoreRepository
                .findByMemberSnoAndSeasonSnoForUpdate(uno, seasonSno)
                .orElseGet(() -> MemberScore.builder()
                        .memberSno(uno)
                        .seasonSno(seasonSno)
                        .totalScore(1500)
                        .build());

        memberScore.addScore(totalScore);
        memberScoreRepository.save(memberScore);
    }
}
