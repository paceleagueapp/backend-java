package com.example.paceleague.board.domain.enums;

public enum VoteType {
    UP(1),
    DOWN(-1);

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
        throw new IllegalArgumentException("voteValue must be 1 or -1");
    }
}
