package com.glennsyj.rivals.api.tft.model.match;

import com.glennsyj.rivals.api.tft.entity.match.TftMatchParticipant;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "TFT 매치 참가자 정보 DTO")
public record TftMatchParticipantDto(
    @Schema(description = "Riot PUUID")
    String puuid,
    @Schema(description = "플레이어 레벨")
    Integer level,
    @Schema(description = "플레이어 최종 순위")
    Integer placement,
    @Schema(description = "플레이어에게 가한 총 피해량")
    Integer totalDamageToPlayers,
    @Schema(description = "Riot ID 게임 이름")
    String riotIdGameName,
    @Schema(description = "Riot ID 태그라인")
    String riotIdTagline,
    @Schema(description = "특성 목록")
    List<TftMatchTrait> traits,
    @Schema(description = "유닛 목록")
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