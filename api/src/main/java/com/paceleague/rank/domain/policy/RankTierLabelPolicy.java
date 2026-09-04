package com.paceleague.rank.domain.policy;

import com.paceleague.common.i18n.Language;
import com.paceleague.rank.domain.enums.RankTier;

import java.util.EnumMap;
import java.util.Map;

// RankTier(BRONZE..CHALLENGER)를 언어별 표시 라벨로 변환하는 순수 조회 테이블.
// web/js/app.js·index.html·post.html에 있던 TIER_LABEL(한국어 고정) 하드코딩을 서버로 옮긴 것 —
// 게임 랭크 UI에서 통용되는 번역(예: LoL 티어명)을 기준으로 채움.
public class RankTierLabelPolicy {

    private static final Map<RankTier, Map<Language, String>> LABELS = build();

    public static String label(RankTier tier, Language lang) {
        return LABELS.get(tier).get(lang);
    }

    private static Map<RankTier, Map<Language, String>> build() {
        Map<RankTier, Map<Language, String>> table = new EnumMap<>(RankTier.class);

        table.put(RankTier.BRONZE, row("브론즈", "Bronze", "ブロンズ", "青铜", "Bronce", "Bronze", "Bronze", "Bronze", "Đồng", "ทองแดง"));
        table.put(RankTier.SILVER, row("실버", "Silver", "シルバー", "白银", "Plata", "Argent", "Silber", "Prata", "Bạc", "เงิน"));
        table.put(RankTier.GOLD, row("골드", "Gold", "ゴールド", "黄金", "Oro", "Or", "Gold", "Ouro", "Vàng", "ทอง"));
        table.put(RankTier.PLATINUM, row("플래티넘", "Platinum", "プラチナ", "铂金", "Platino", "Platine", "Platin", "Platina", "Bạch Kim", "แพลทินัม"));
        table.put(RankTier.DIAMOND, row("다이아", "Diamond", "ダイヤ", "钻石", "Diamante", "Diamant", "Diamant", "Diamante", "Kim Cương", "เพชร"));
        table.put(RankTier.MASTER, row("마스터", "Master", "マスター", "大师", "Maestro", "Maître", "Meister", "Mestre", "Cao Thủ", "มาสเตอร์"));
        table.put(RankTier.CHALLENGER, row("챌린저", "Challenger", "チャレンジャー", "挑战者", "Retador", "Challenger", "Herausforderer", "Desafiante", "Thách Đấu", "ชาเลนเจอร์"));

        return table;
    }

    private static Map<Language, String> row(
            String ko, String en, String ja, String zh, String es,
            String fr, String de, String pt, String vi, String th
    ) {
        Map<Language, String> row = new EnumMap<>(Language.class);
        row.put(Language.KO, ko);
        row.put(Language.EN, en);
        row.put(Language.JA, ja);
        row.put(Language.ZH, zh);
        row.put(Language.ES, es);
        row.put(Language.FR, fr);
        row.put(Language.DE, de);
        row.put(Language.PT, pt);
        row.put(Language.VI, vi);
        row.put(Language.TH, th);
        return row;
    }
}
