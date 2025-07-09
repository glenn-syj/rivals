package com.glennsyj.rivals.api.tft.model.match;

import com.glennsyj.rivals.api.tft.entity.match.TftMatch;
import com.glennsyj.rivals.api.tft.entity.match.TftMatchParticipant;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

/*
    특정 계정의 전적 전시에 이용될 DTO
 */
@Schema(description = "최근 TFT 매치 정보 DTO")
public record TftRecentMatchDto(
    @Schema(description = "매치 ID (내부)")
    String id,
    @Schema(description = "Riot API 매치 ID")
    String matchId,
    @Schema(description = "게임 생성 시간 (에포크 시간)")
    Long gameCreation,
    @Schema(description = "게임 길이 (초)")
    Double gameLength,
    @Schema(description = "플레이어 레벨")
    Integer level,
    @Schema(description = "플레이어 최종 순위")
    Integer placement,
    @Schema(description = "큐 타입 (예: RANKED_TFT_TURBO)")
    String queueType,
    @Schema(description = "특성 목록")
    List<TftMatchTrait> traits,
    @Schema(description = "유닛 목록")
    List<TftMatchUnit> units,
    @Schema(description = "참가자 목록")
    List<TftMatchParticipantDto> participants
) {
    public static TftRecentMatchDto from(String puuid, TftMatch match) {
        TftMatchParticipant matchParticipant = match.getParticipantByPuuid(puuid);
        List<TftMatchParticipantDto> participants = match.getParticipants().stream()
            .map(TftMatchParticipantDto::from)
            .toList();

        return new TftRecentMatchDto(
            match.getId().toString(),
            match.getMatchId(),
            match.getGameCreation(),
            match.getGameLength(),
            matchParticipant.getLevel(),
            matchParticipant.getPlacement(),
            match.getTftGameType(),
            matchParticipant.getTraits(),
            matchParticipant.getUnits(),
            participants
        );
    }
}
