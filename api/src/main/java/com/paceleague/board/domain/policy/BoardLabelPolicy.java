package com.paceleague.board.domain.policy;

import com.paceleague.common.i18n.Language;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// board.name/description을 언어별로 번역해 보여주기 위한 순수 조회 테이블. slug(free/qna/verify)로 찾는다.
// 한국어는 DB에 이미 원본(한국어) 값이 들어있으므로 여기 따로 두지 않고 항상 DB 값(fallback)을 그대로 쓴다 —
// 두 곳에 같은 한국어 원문을 중복 유지하지 않기 위함. 미등록 slug(향후 보드 추가 시)나 KO는 fallback으로 처리.
public class BoardLabelPolicy {

    public record Labels(String name, String description) {}

    private static final Map<String, Map<Language, Labels>> TABLE = build();

    public static String name(String slug, Language lang, String fallback) {
        return lookup(slug, lang).map(Labels::name).orElse(fallback);
    }

    public static String description(String slug, Language lang, String fallback) {
        return lookup(slug, lang).map(Labels::description).orElse(fallback);
    }

    private static Optional<Labels> lookup(String slug, Language lang) {
        if (lang == Language.KO) {
            return Optional.empty();
        }
        Map<Language, Labels> row = TABLE.get(slug);
        return row == null ? Optional.empty() : Optional.ofNullable(row.get(lang));
    }

    private static Map<String, Map<Language, Labels>> build() {
        Map<String, Map<Language, Labels>> table = new HashMap<>();

        table.put("free", row(
                new Labels("Free Talk", "A space to chat freely."),
                new Labels("自由掲示板", "自由に話せる空間です。"),
                new Labels("自由讨论", "自由交流的空间。"),
                new Labels("Charla libre", "Un espacio para hablar libremente."),
                new Labels("Discussion libre", "Un espace pour discuter librement."),
                new Labels("Freies Forum", "Ein Ort für freien Austausch."),
                new Labels("Conversa livre", "Um espaço para conversar livremente."),
                new Labels("Tự do", "Không gian trò chuyện tự do."),
                new Labels("พูดคุยทั่วไป", "พื้นที่พูดคุยอย่างอิสระ")
        ));

        table.put("qna", row(
                new Labels("Q&A", "Ask your running-related questions."),
                new Labels("質問", "ランニングに関する質問を投稿してください。"),
                new Labels("问答", "请提出与跑步相关的问题。"),
                new Labels("Preguntas", "Publica tus preguntas sobre running."),
                new Labels("Questions", "Posez vos questions sur la course."),
                new Labels("Fragen", "Stelle deine Fragen rund ums Laufen."),
                new Labels("Perguntas", "Poste suas perguntas sobre corrida."),
                new Labels("Hỏi đáp", "Đăng câu hỏi liên quan đến chạy bộ."),
                new Labels("ถาม-ตอบ", "โพสต์คำถามเกี่ยวกับการวิ่ง")
        ));

        table.put("verify", row(
                new Labels("Verify", "Verify today's run."),
                new Labels("認証", "今日のランニングを認証してみましょう。"),
                new Labels("打卡", "打卡今天的跑步。"),
                new Labels("Verificación", "Verifica tu carrera de hoy."),
                new Labels("Vérification", "Vérifiez votre course du jour."),
                new Labels("Verifizierung", "Bestätige deinen heutigen Lauf."),
                new Labels("Verificação", "Verifique sua corrida de hoje."),
                new Labels("Xác nhận", "Xác nhận buổi chạy hôm nay."),
                new Labels("ยืนยัน", "ยืนยันการวิ่งของวันนี้")
        ));

        table.put("crew_promo", row(
                new Labels("Crew Recruit", "Introduce your crew and recruit members."),
                new Labels("クルー募集", "自分のクルーを紹介してメンバーを募集しましょう。"),
                new Labels("战队招募", "介绍你的战队并招募成员。"),
                new Labels("Reclutar crew", "Presenta tu crew y recluta miembros."),
                new Labels("Recrutement de crew", "Présentez votre crew et recrutez des membres."),
                new Labels("Crew-Werbung", "Stelle deine Crew vor und wirb Mitglieder."),
                new Labels("Recrutar crew", "Apresente sua crew e recrute membros."),
                new Labels("Tuyển crew", "Giới thiệu crew của bạn và tuyển thành viên."),
                new Labels("รับสมัครครู", "แนะนำครูของคุณและรับสมาชิก")
        ));

        return table;
    }

    private static Map<Language, Labels> row(
            Labels en, Labels ja, Labels zh, Labels es,
            Labels fr, Labels de, Labels pt, Labels vi, Labels th
    ) {
        Map<Language, Labels> row = new HashMap<>();
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
