package com.glennsyj.rivals.api.tft.model.entry;

import com.glennsyj.rivals.api.tft.entity.entry.TftLeagueEntry;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * TFT 상태 정보 DTO
 */
@Schema(description = "TFT 리그 엔트리 상태 정보 DTO")
public record TftStatusDto(
        @Schema(description = "큐 타입 (예: RANKED_TFT_TURBO)")
        String queueType,         // 큐 타입 (Enum QueueType)
        @Schema(description = "티어 (예: DIAMOND)")
        String tier,              // 티어 (e.g. DIAMOND)
        @Schema(description = "랭크 (예: I, II, III, IV)")
        String rank,              // 랭크 (e.g. I, II, III, IV)
        @Schema(description = "리그 포인트 (LP)")
        int leaguePoints,         // LP
        @Schema(description = "1-4등 횟수")
        int wins,                 // 1-4등 횟수
        @Schema(description = "5-8등 횟수")
        int losses,               // 5-8등 횟수
        @Schema(description = "연승 여부")
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
