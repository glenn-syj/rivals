package com.glennsyj.rivals.api.tft.entity.match;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.tft.model.match.TftMatchInfo;
import com.glennsyj.rivals.api.tft.model.match.TftMatchMetadata;
import com.glennsyj.rivals.api.tft.model.match.TftMatchResponse;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "tft_matches",
        indexes = {
                @Index(name = "idx_match_id", columnList = "matchId", unique = true)
        })
public class TftMatch {
    @Id
    @Tsid
    private Long id;

    @Column(nullable = false, unique = true)
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

    @OneToMany(mappedBy = "match", cascade = CascadeType.PERSIST)
    private List<TftMatchParticipant> participants = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

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

    public TftMatchParticipant getParticipantByPuuid(String puuid) {
        for (TftMatchParticipant participant : participants) {
            if (participant.getPuuid().equals(puuid)) {
                return participant;
            }
        }

        return null;
    }

    // Participant management
    public void addParticipant(TftMatchParticipant participant) {
        participants.add(participant);
        participant.setMatch(this);
    }

    public static TftMatch from(TftMatchResponse response) {
        TftMatchMetadata metadata = response.metadata();
        TftMatchInfo info = response.info();

        TftMatch match = new TftMatch(
            metadata.match_id(),
            metadata.data_version(),
            info.gameCreation(),
            info.gameId(),
            info.game_datetime(),
            info.game_length(),
            info.game_version(),
            info.game_variation(),
            info.mapId(),
            info.queueId(),
            info.tft_game_type(),
            info.tft_set_core_name(),
            info.tft_set_number(),
            info.endOfGameResult()
        );

        // 참가자 추가
        for (com.glennsyj.rivals.api.tft.model.match.TftMatchParticipant participant : info.participants()) {
            match.addParticipant(TftMatchParticipant.from(participant));
        }

        return match;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TftMatch that)) return false;
        TftMatch actual = that instanceof HibernateProxy proxy
                ? (TftMatch) proxy.getHibernateLazyInitializer().getImplementation()
                : that;

        return matchId.equals(actual.matchId);
    }

    @Override
    public int hashCode() {
        return getMatchId() != null ? getMatchId().hashCode() : 0;
    }
}