package com.glennsyj.rivals.api.tft.entity.match;

import com.glennsyj.rivals.api.tft.model.match.TftMatchCompanion;
import com.glennsyj.rivals.api.tft.model.match.TftMatchTrait;
import com.glennsyj.rivals.api.tft.model.match.TftMatchUnit;
import io.hypersistence.utils.hibernate.id.Tsid;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import org.hibernate.proxy.HibernateProxy;

import java.util.List;

@Entity
@Table(name = "tft_match_participants")
public class TftMatchParticipant {
    @Id
    @Tsid
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private TftMatch match;

    @Column(nullable = false)
    private String puuid;

    @Column(name = "gold_left", nullable = false)
    private Integer goldLeft;

    @Column(name = "last_round", nullable = false)
    private Integer lastRound;

    // Riot API에서 동일하게 뒤에 2붙임
    @Column(name = "missions_player_score2", nullable = false)
    private Integer missionsPlayerScore2;

    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false)
    private Integer placement;

    @Column(name = "players_eliminated", nullable = false)
    private Integer playersEliminated;

    @Column(name = "riot_id_game_name", nullable = false)
    private String riotIdGameName;

    @Column(name = "riot_id_tagline", nullable = false)
    private String riotIdTagline;

    @Column(name = "time_eliminated", nullable = false)
    private Double timeEliminated;

    @Column(name = "total_damage_to_players", nullable = false)
    private Integer totalDamageToPlayers;

    @Column(nullable = false)
    private Boolean win;

    @Type(JsonType.class)
    @Column(columnDefinition = "JSON")
    private TftMatchCompanion companion;

    @Type(JsonType.class)
    @Column(columnDefinition = "JSON")
    private List<TftMatchTrait> traits;

    @Type(JsonType.class)
    @Column(columnDefinition = "JSON")
    private List<TftMatchUnit> units;

    protected TftMatchParticipant() {}

    public TftMatchParticipant(String puuid, Integer goldLeft, Integer lastRound,
                               Integer missionsPlayerScore2, Integer level,
                               Integer placement, Integer playersEliminated, String riotIdGameName,
                               String riotIdTagline, Double timeEliminated, Integer totalDamageToPlayers,
                               Boolean win, TftMatchCompanion companion,
                               List<TftMatchTrait> traits, List<TftMatchUnit> units) {
        this.puuid = puuid;
        this.goldLeft = goldLeft;
        this.lastRound = lastRound;
        this.missionsPlayerScore2 = missionsPlayerScore2;
        this.level = level;
        this.placement = placement;
        this.playersEliminated = playersEliminated;
        this.riotIdGameName = riotIdGameName;
        this.riotIdTagline = riotIdTagline;
        this.timeEliminated = timeEliminated;
        this.totalDamageToPlayers = totalDamageToPlayers;
        this.win = win;
        this.companion = companion;
        this.traits = traits;
        this.units = units;
    }

    // Getters
    public Long getId() { return id; }
    public TftMatch getMatch() { return match; }
    public String getPuuid() { return puuid; }
    public Integer getGoldLeft() { return goldLeft; }
    public Integer getLastRound() { return lastRound; }
    public Integer getLevel() { return level; }
    public Integer getPlacement() { return placement; }
    public Integer getMissionsPlayerScore2() { return missionsPlayerScore2; }
    public Integer getPlayersEliminated() { return playersEliminated; }
    public String getRiotIdGameName() { return riotIdGameName; }
    public String getRiotIdTagline() { return riotIdTagline; }
    public Double getTimeEliminated() { return timeEliminated; }
    public Integer getTotalDamageToPlayers() { return totalDamageToPlayers; }
    public Boolean getWin() { return win; }
    public TftMatchCompanion getCompanion() { return companion; }
    public List<TftMatchTrait> getTraits() { return traits; }
    public List<TftMatchUnit> getUnits() { return units; }

    // Setter for relationship
    void setMatch(TftMatch match) { this.match = match; }

    public static TftMatchParticipant from(com.glennsyj.rivals.api.tft.model.match.TftMatchParticipant participantResponse) {
        return new TftMatchParticipant(
            participantResponse.puuid(),
            participantResponse.gold_left(),
            participantResponse.last_round(),
            participantResponse.missions().get("PlayerScore2"),
            participantResponse.level(),
            participantResponse.placement(),
            participantResponse.players_eliminated(),
            participantResponse.riotIdGameName(),
            participantResponse.riotIdTagline(),
            participantResponse.time_eliminated(),
            participantResponse.total_damage_to_players(),
            participantResponse.win(),
            participantResponse.companion(),
            participantResponse.traits(),
            participantResponse.units()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TftMatchParticipant that)) return false;
        TftMatchParticipant actual = that instanceof HibernateProxy proxy
                ? (TftMatchParticipant) proxy.getHibernateLazyInitializer().getImplementation()
                : that;

        return puuid.equals(actual.puuid) && match.getMatchId().equals(actual.match.getMatchId());
    }

    @Override
    public int hashCode() {
        int result = puuid != null ? puuid.hashCode() : 0;
        result = 31 * result + (match != null ? match.getMatchId().hashCode() : 0);
        return result;
    }
} 