package com.glennsyj.rivals.api.tft.entity.match;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tft_matches")
public class TftMatch {
    @Id
    @Tsid
    private Long id;

    @Column(nullable = false)
    private String matchId;  // from metadata

    @Column(nullable = false)
    private String dataVersion;  // from metadata

    @Column(nullable = false)
    private String endOfGameResult;

    @Column(nullable = false)
    private Long gameCreation;

    @Column(nullable = false)
    private Long gameId;

    @Column(nullable = false)
    private Long gameDateTime;

    @Column(nullable = false)
    private Double gameLength;

    @Column(nullable = false)
    private String gameVersion;

    @Column(nullable = false)
    private Integer mapId;

    @Column(name = "game_variation")
    private String gameVariation;

    @Column(nullable = false)
    private Integer queueId;

    @Column(nullable = false)
    private String tftGameType;

    @Column(nullable = false)
    private String tftSetCoreName;

    @Column(nullable = false)
    private Integer tftSetNumber;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TftMatchParticipant> participants = new ArrayList<>();

    protected TftMatch() {}

    public TftMatch(String matchId, String dataVersion, Long gameCreation, Long gameId,
                   Long gameDateTime, Double gameLength, String gameVersion, String gameVariation, 
                   Integer mapId, Integer queueId, String tftGameType, String tftSetCoreName,
                   Integer tftSetNumber, String endOfGameResult) {
        this.matchId = matchId;
        this.dataVersion = dataVersion;
        this.gameCreation = gameCreation;
        this.gameId = gameId;
        this.gameDateTime = gameDateTime;
        this.gameLength = gameLength;
        this.gameVersion = gameVersion;
        this.gameVariation = gameVariation;
        this.mapId = mapId;
        this.queueId = queueId;
        this.tftGameType = tftGameType;
        this.tftSetCoreName = tftSetCoreName;
        this.tftSetNumber = tftSetNumber;
        this.endOfGameResult = endOfGameResult;
    }

    // Getters
    public Long getId() { return id; }
    public String getMatchId() { return matchId; }
    public String getDataVersion() { return dataVersion; }
    public Long getGameCreation() { return gameCreation; }
    public Long getGameId() { return gameId; }
    public Long getGameDateTime() { return gameDateTime; }
    public Double getGameLength() { return gameLength; }
    public String getGameVersion() { return gameVersion; }
    public String getGameVariation() { return gameVariation; }
    public Integer getMapId() { return mapId; }
    public Integer getQueueId() { return queueId; }
    public String getTftGameType() { return tftGameType; }
    public String getTftSetCoreName() { return tftSetCoreName; }
    public Integer getTftSetNumber() { return tftSetNumber; }
    public String getEndOfGameResult() { return endOfGameResult; }
    public List<TftMatchParticipant> getParticipants() { return participants; }

    // Participant management
    public void addParticipant(TftMatchParticipant participant) {
        participants.add(participant);
        participant.setMatch(this);
    }
}