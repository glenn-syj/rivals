package com.glennsyj.rivals.api.riot.entity;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "riot_accounts",
        indexes = {
                @Index(
                        name = "idx_riot_accounts_game_name_tag_line",
                        columnList = "game_name,tag_line",
                        unique = true
                )
        }
)
@EntityListeners(AuditingEntityListener.class)
public class RiotAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id = UlidCreator.getUlid().toUuid();

    @Column(name = "game_name", nullable = false)
    private String gameName;

    @Column(name = "tag_line", nullable = false)
    private String tagLine;

    @Column(nullable = false, unique = true)
    private String puuid;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected RiotAccount() {}

    public RiotAccount(String gameName, String tagLine, String puuid) {
        this.gameName = gameName;
        this.tagLine = tagLine;
        this.puuid = puuid;
    }
}