package com.glennsyj.rivals.api.riot.entity;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "riot_accounts",
        indexes = {
                @Index(
                        name = "idx_riot_accounts_game_name_tag_line",
                        columnList = "game_name,tag_line"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_riot_accounts_puuid",  // 명시적인 이름
                        columnNames = "puuid"
                )
        }
)
@EntityListeners(AuditingEntityListener.class)
public class RiotAccount {
    @Id
    @Tsid
    private Long id;

    @Column(name = "game_name", nullable = false)
    private String gameName;

    @Column(name = "tag_line", nullable = false)
    private String tagLine;

    // puuid 필드는 항상 nullable 하므로
    @Column(nullable = false, unique = true)
    private String puuid;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected RiotAccount() {}

    public RiotAccount(String gameName, String tagLine, String puuid) {
        this.gameName = gameName;
        this.tagLine = tagLine;
        this.puuid = puuid;
    }

    public long getId() {
        return id;
    }

    public String getPuuid() {
        return puuid;
    }

    public String getGameName() {
        return gameName;
    }

    public String getTagLine() {
        return tagLine;
    }

    public String getFullGameName() {
        return String.format("%s#%s", gameName, tagLine);
    }

    public void updateGameIdentity(String newGameName, String newTagLine) {
        this.gameName = newGameName;
        this.tagLine = newTagLine;
    }

    private String getPuuidNoProxy() {
        if (this instanceof HibernateProxy) {
            LazyInitializer lazyInitializer =
                    ((HibernateProxy) this).getHibernateLazyInitializer();
            if (!lazyInitializer.isUninitialized()) {
                return ((RiotAccount) lazyInitializer.getImplementation()).puuid;
            }
            return null;
        }
        return this.puuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RiotAccount that)) return false;

        RiotAccount actual = that instanceof HibernateProxy proxy
                ? (RiotAccount) proxy.getHibernateLazyInitializer().getImplementation()
                : that;

        return puuid.equals(actual.puuid);
    }

    @Override
    public int hashCode() {
        return getPuuid() != null ? getPuuid().hashCode() : 0;
    }
}