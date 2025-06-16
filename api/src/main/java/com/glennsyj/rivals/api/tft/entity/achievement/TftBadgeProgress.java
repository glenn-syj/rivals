package com.glennsyj.rivals.api.tft.entity.achievement;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tft_badge_progress",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"puuid", "badge_type"})
        })
public class TftBadgeProgress {

    @Id
    @Tsid
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "riot_account_id", nullable = false)
    private RiotAccount riotAccount;  // puuid 대신 RiotAccount 참조

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BadgeType badgeType;

    @Column(nullable = false)
    private Integer achievementCount;  // 최근 N경기 내 업적 달성 횟수

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt;

    protected TftBadgeProgress() {}

    public TftBadgeProgress(RiotAccount riotAccount, BadgeType badgeType) {
        this.riotAccount = riotAccount;
        this.badgeType = badgeType;
        this.achievementCount = 0;
        this.isActive = false;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public void updateProgress(int newCount) {
        this.achievementCount = newCount;
        this.isActive = newCount >= badgeType.getRequiredCount();
        this.lastUpdatedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public RiotAccount getRiotAccount() { return riotAccount; }
    public BadgeType getBadgeType() { return badgeType; }
    public Integer getAchievementCount() { return achievementCount; }
    public Boolean isActive() { return isActive; }
    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }

    public enum BadgeType {
        LUXURY(AchievementType.MOST_EXPENSIVE_SQUAD, 5),
        DAMAGE_DEALER(AchievementType.MOST_DAMAGE_DEALT, 5),
        EXECUTOR(AchievementType.MOST_ELIMINATIONS, 5),
        MVP(AchievementType.FIRST_PLACE, 6),
        STEADY(AchievementType.TOP_FOUR, 10);

        private final AchievementType achievementType;
        private final int requiredCount;

        BadgeType(AchievementType achievementType, int requiredCount) {
            this.achievementType = achievementType;
            this.requiredCount = requiredCount;
        }

        public AchievementType getAchievementType() {
            return achievementType;
        }

        public int getRequiredCount() {
            return requiredCount;
        }
    }
}
