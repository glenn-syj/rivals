package com.glennsyj.rivals.api.tft.model.match;

import java.util.List;

/*
    특정 계정의 전적 전시에 이용될 DTO
 */
public record TftRecentMatchDto(
    String id,
    String matchId,
    Long gameCreation,
    Long gameLength,
    Integer level,
    String queueType,
    List<TftMatchTrait> traits,
    List<TftMatchUnit> units
) {
}
