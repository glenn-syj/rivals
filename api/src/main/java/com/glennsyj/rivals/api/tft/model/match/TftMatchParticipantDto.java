package com.glennsyj.rivals.api.tft.model.match;

import com.glennsyj.rivals.api.tft.entity.match.TftMatchParticipant;

import java.util.List;

public record TftMatchParticipantDto(
    String puuid,
    Integer level,
    Integer placement,
    Integer totalDamageToPlayers,
    String riotIdGameName,
    String riotIdTagline,
    List<TftMatchTrait> traits,
    List<TftMatchUnit> units
) {
    public static TftMatchParticipantDto from(TftMatchParticipant participant) {
        return new TftMatchParticipantDto(
            participant.getPuuid(),
            participant.getLevel(),
            participant.getPlacement(),
            participant.getTotalDamageToPlayers(),
            participant.getRiotIdGameName(),
            participant.getRiotIdTagline(),
            participant.getTraits(),
            participant.getUnits()
        );
    }
} 