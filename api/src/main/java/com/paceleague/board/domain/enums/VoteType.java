package com.paceleague.board.domain.enums;

// 2026-10-27: 추천/비추천 → 좋아요 하나로 변경(비추천 폐지). 같은 값을 다시 보내면 좋아요 취소.
public enum VoteType {
    UP(1);

    private final int value;

    VoteType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static VoteType fromValue(int value) {
        for (VoteType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("voteValue must be 1");
    }
}
