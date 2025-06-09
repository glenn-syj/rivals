package com.glennsyj.rivals.api.tft.entity.entry;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.tft.model.entry.TftLeagueEntryResponse;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tft_league_entries"
)
public class TftLeagueEntry {
    @Id
    @Tsid
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private RiotAccount account;

    @Column(nullable = false)
    private String puuid;

    @Column(name = "league_id", nullable = false)
    private String leagueId;

    @Column(name = "summoner_id", nullable = false)
    private String summonerId;

    @Column(name = "queue_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private QueueType queueType;

    @Enumerated(EnumType.STRING)
    private Tier tier;

    @Enumerated(EnumType.STRING)
    private Rank rank;

    @Column(name = "league_points")
    private int leaguePoints;

    private int wins;
    private int losses;

    @Column(name = "hot_streak")
    private boolean hotStreak;

    private boolean veteran;

    @Column(name = "fresh_blood")
    private boolean freshBlood;

    private boolean inactive;

    @Embedded
    private MiniSeries miniSeries;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected TftLeagueEntry() {}

    public TftLeagueEntry(RiotAccount account, TftLeagueEntryResponse response) {
        validatePuuid(account, response);
        this.account = account;
        this.puuid = account.getPuuid();
        this.leagueId = response.leagueId();
        this.summonerId = response.summonerId();
        this.queueType = QueueType.valueOf(response.queueType());
        this.tier = Tier.valueOf(response.tier());
        this.rank = Rank.valueOf(response.rank());
        this.leaguePoints = response.leaguePoints();
        this.wins = response.wins();
        this.losses = response.losses();
        this.hotStreak = response.hotStreak();
        this.veteran = response.veteran();
        this.freshBlood = response.freshBlood();
        this.inactive = response.inactive();
        this.miniSeries = response.miniSeries() != null ?
                new MiniSeries(response.miniSeries()) : null;
    }

    public TftLeagueEntryResponse toDto() {
        return new TftLeagueEntryResponse(
                account.getPuuid(),
                leagueId,
                summonerId,
                queueType.name(),
                tier.name(),
                rank.name(),
                leaguePoints,
                wins,
                losses,
                hotStreak,
                veteran,
                freshBlood,
                inactive,
                miniSeries != null ? miniSeries.toResponse() : null
        );
    }

    public void updateFrom(TftLeagueEntryResponse response) {
        validatePuuid(this.account, response);
        this.leagueId = response.leagueId();
        this.summonerId = response.summonerId();
        this.queueType = QueueType.valueOf(response.queueType());
        this.tier = Tier.valueOf(response.tier());
        this.rank = Rank.valueOf(response.rank());
        this.leaguePoints = response.leaguePoints();
        this.wins = response.wins();
        this.losses = response.losses();
        this.hotStreak = response.hotStreak();
        this.veteran = response.veteran();
        this.freshBlood = response.freshBlood();
        this.inactive = response.inactive();
        this.miniSeries = response.miniSeries() != null ? 
            new MiniSeries(response.miniSeries()) : null;
    }

    private void validatePuuid(RiotAccount account, TftLeagueEntryResponse response) {
        if (!account.getPuuid().equals(response.puuid())) {
            throw new IllegalArgumentException(
                "PUUID 불일치: account=" + account.getPuuid() + ", response=" + response.puuid()
            );
        }
    }

    public enum QueueType {
        RANKED_TFT,
        RANKED_TFT_TURBO,
        RANKED_TFT_DOUBLE_UP
    }

    public enum Tier {
        IRON, BRONZE, SILVER, GOLD, PLATINUM, DIAMOND,
        MASTER, GRANDMASTER, CHALLENGER
    }

    public enum Rank {
        I, II, III, IV
    }

    public Long getId() {
        return id;
    }

    public RiotAccount getAccount() {
        return account;
    }

    public String getPuuid() {
        return puuid;
    }

    public String getLeagueId() {
        return leagueId;
    }

    public String getSummonerId() {
        return summonerId;
    }

    public QueueType getQueueType() {
        return queueType;
    }

    public Tier getTier() {
        return tier;
    }

    public Rank getRank() {
        return rank;
    }

    public int getLeaguePoints() {
        return leaguePoints;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public boolean isHotStreak() {
        return hotStreak;
    }

    public boolean isVeteran() {
        return veteran;
    }

    public boolean isFreshBlood() {
        return freshBlood;
    }

    public boolean isInactive() {
        return inactive;
    }

    public MiniSeries getMiniSeries() {
        return miniSeries;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
