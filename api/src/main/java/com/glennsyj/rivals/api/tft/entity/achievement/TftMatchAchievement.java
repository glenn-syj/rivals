package com.glennsyj.rivals.api.tft.entity.achievement;

import com.glennsyj.rivals.api.tft.entity.match.TftMatch;
import com.glennsyj.rivals.api.tft.entity.match.TftMatchParticipant;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;

@Entity
@Table(name = "tft_match_achievements")
public class TftMatchAchievement {
    @Id
    @Tsid
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private TftMatch match;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AchievementType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private TftMatchParticipant achiever;

    @Column(nullable = false)
    private Integer value;  // 해당 업적의 수치 (데미지량, 등수, 가격 등)

    public enum AchievementType {
        MOST_EXPENSIVE_SQUAD,
        MOST_DAMAGE_DEALT,
        MOST_ELIMINATIONS,
        FIRST_PLACE,
        TOP_FOUR
    }

    protected TftMatchAchievement() {}

    public TftMatchAchievement(TftMatch match, AchievementType type, TftMatchParticipant achiever, Integer value) {
        this.match = match;
        this.type = type;
        this.achiever = achiever;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public TftMatch getMatch() {
        return match;
    }

    public AchievementType getType() {
        return type;
    }

    public TftMatchParticipant getAchiever() {
        return achiever;
    }

    public Integer getValue() {
        return value;
    }
}
