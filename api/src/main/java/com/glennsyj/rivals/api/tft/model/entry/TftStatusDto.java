package com.glennsyj.rivals.api.tft.model.entry;

import com.glennsyj.rivals.api.tft.entity.entry.TftLeagueEntry;

/**
 * TFT 상태 정보 DTO
 */
public record TftStatusDto(
        String queueType,         // 큐 타입 (Enum QueueType)
        String tier,              // 티어 (e.g. DIAMOND)
        String rank,              // 랭크 (e.g. I, II, III, IV)
        int leaguePoints,         // LP
        int wins,                 // 1등 횟수
        int losses,               // 2-8등 횟수
        boolean hotStreak         // 연승 여부
) {

    // TftLeagueEntry로부터 TftStatusDto를 생성
    public static TftStatusDto from(TftLeagueEntry entry) {
        return new TftStatusDto(
                entry.getQueueType().name(),
                entry.getTier().name(),
                entry.getRank().name(),
                entry.getLeaguePoints(),
                entry.getWins(),
                entry.getLosses(),
                entry.isHotStreak()
        );
    }
}
