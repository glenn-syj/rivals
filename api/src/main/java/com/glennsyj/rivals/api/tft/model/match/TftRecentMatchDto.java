package com.glennsyj.rivals.api.tft.model.match;

import com.glennsyj.rivals.api.tft.entity.match.TftMatch;
import com.glennsyj.rivals.api.tft.entity.match.TftMatchParticipant;

import java.util.List;

/*
    특정 계정의 전적 전시에 이용될 DTO
 */
public record TftRecentMatchDto(
    String id,
    String matchId,
    Long gameCreation,
    Double gameLength,
    Integer level,
    String queueType,
    List<TftMatchTrait> traits,
    List<TftMatchUnit> units
) {
    public static TftRecentMatchDto from(String puuid, TftMatch match) {
        TftMatchParticipant matchParticipant = match.getParticipantByPuuid(puuid);

        return new TftRecentMatchDto(
            match.getId().toString(),
            match.getMatchId(),
            match.getGameCreation(),
            match.getGameLength(),
            matchParticipant.getLevel(),
            match.getTftGameType(),
            matchParticipant.getTraits(),
            matchParticipant.getUnits()
        );
    }
}
