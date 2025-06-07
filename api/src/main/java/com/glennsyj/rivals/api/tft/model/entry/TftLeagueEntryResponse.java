package com.glennsyj.rivals.api.tft.model.entry;

/**
 * TFT 랭크 정보 응답 DTO
 * RANKED_TFT_TURBO(하이퍼롤) 제외한 일반 TFT 랭크 정보
 */
public record TftLeagueEntryResponse(
        String puuid,              // 플레이어 고유 식별자 (78자)
        String leagueId,          // 리그 고유 식별자
        String summonerId,        // 암호화된 소환사 ID
        String queueType,         // 큐 타입 (e.g. RANKED_TFT)
        String tier,              // 티어 (e.g. DIAMOND)
        String rank,              // 랭크 (e.g. I, II, III, IV)
        int leaguePoints,         // LP
        int wins,                 // 1등 횟수
        int losses,               // 2-8등 횟수
        boolean hotStreak,        // 연승 여부
        boolean veteran,          // 베테랑 여부
        boolean freshBlood,       // 새로운 플레이어 여부
        boolean inactive,         // 휴면 계정 여부
        MiniSeries miniSeries    // 승급전 정보 (있는 경우)
) {
    /**
     * 승급전 정보
     */
    public record MiniSeries(
            int losses,           // 승급전 패배 수
            int target,           // 승급전 목표 승수
            int wins,             // 승급전 승리 수
            String progress       // 승급전 진행 상황 (e.g. "WLN")
    ) {}
}
