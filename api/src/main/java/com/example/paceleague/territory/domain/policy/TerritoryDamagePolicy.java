package com.example.paceleague.territory.domain.policy;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 땅따먹기 데미지/회복/점령자 판정의 순수 계산 로직.
//  - 겹친 면적 비례로 데미지·회복량을 산정한다(겹친 면적 / 대상 면적 비율 × maxHp × 계수).
//  - HP가 0이 되면 1시간 윈도우 내 기여도 합이 가장 큰 사람이 점령한다(동점 → 가장 최근 기여 → 이번 공격자).
public final class TerritoryDamagePolicy {

    private TerritoryDamagePolicy() {
    }

    // 1시간 윈도우 기여도 한 건. 정책을 순수하게 유지하려고 엔티티 대신 이 레코드를 받는다.
    public record Contribution(Long memberSno, int damage, LocalDateTime at) {
    }

    public static int damage(double overlapSqm, double targetAreaSqm, int targetMaxHp, double attackFactor) {
        if (overlapSqm <= 0 || targetAreaSqm <= 0 || targetMaxHp <= 0) {
            return 0;
        }
        double ratio = Math.min(1.0, overlapSqm / targetAreaSqm);
        int dmg = (int) Math.round(ratio * targetMaxHp * attackFactor);
        return Math.max(1, dmg);
    }

    public static int heal(double overlapSqm, double myAreaSqm, int maxHp, double healFactor) {
        if (overlapSqm <= 0 || myAreaSqm <= 0 || maxHp <= 0) {
            return 0;
        }
        double ratio = Math.min(1.0, overlapSqm / myAreaSqm);
        int amount = (int) Math.round(ratio * maxHp * healFactor);
        return Math.max(1, amount);
    }

    // priorWindow: HP를 깎기 직전에 조회한 1시간 윈도우 기여도(이번 러닝 제외).
    // currentAttacker/currentDamage/currentAt: 이번 러닝이 이 땅에 넣은 마지막 타격.
    public static Long resolveNewOwner(List<Contribution> priorWindow,
                                       Long currentAttacker, int currentDamage, LocalDateTime currentAt) {
        Map<Long, Integer> totalDamage = new HashMap<>();
        Map<Long, LocalDateTime> latestAt = new HashMap<>();

        for (Contribution c : priorWindow) {
            accumulate(totalDamage, latestAt, c.memberSno(), c.damage(), c.at());
        }
        accumulate(totalDamage, latestAt, currentAttacker, currentDamage, currentAt);

        Long best = currentAttacker;
        for (Map.Entry<Long, Integer> entry : totalDamage.entrySet()) {
            Long candidate = entry.getKey();
            if (candidate.equals(best)) {
                continue;
            }
            int cmp = Integer.compare(entry.getValue(), totalDamage.get(best));
            if (cmp > 0 || (cmp == 0 && latestAt.get(candidate).isAfter(latestAt.get(best)))) {
                best = candidate;
            }
        }
        return best;
    }

    private static void accumulate(Map<Long, Integer> totalDamage, Map<Long, LocalDateTime> latestAt,
                                   Long memberSno, int damage, LocalDateTime at) {
        totalDamage.merge(memberSno, damage, Integer::sum);
        latestAt.merge(memberSno, at, (a, b) -> a.isAfter(b) ? a : b);
    }
}
