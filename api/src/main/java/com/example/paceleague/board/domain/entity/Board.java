package com.example.paceleague.board.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "board", uniqueConstraints = @UniqueConstraint(columnNames = "slug"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sno;

    @Column(nullable = false, length = 50)
    private String slug;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    public Board(String slug, String name, String description, int displayOrder) {
        this.slug = slug;
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
        this.createAt = LocalDateTime.now(ZoneOffset.UTC);
        this.updateAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public static Board create(String slug, String name, String description, int displayOrder) {
        return new Board(slug, name, description, displayOrder);
    }

    @PreUpdate
    public void preUpdate() {
        this.updateAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
