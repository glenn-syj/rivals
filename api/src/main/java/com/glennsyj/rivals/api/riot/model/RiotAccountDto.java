package com.glennsyj.rivals.api.riot.model;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Riot 계정 응답 DTO")
public record RiotAccountDto(
        @Schema(description = "Riot PUUID")
        String puuid,
        @Schema(description = "게임 내 이름")
        String gameName,
        @Schema(description = "태그 라인")
        String tagLine,
        @Schema(description = "계정 ID")
        String id,
        @Schema(description = "마지막 업데이트 시간")
        LocalDateTime updatedAt
) {
    public static RiotAccountDto from(RiotAccount account) {
        return new RiotAccountDto(
            account.getPuuid(),
            account.getGameName(),
            account.getTagLine(),
            String.valueOf(account.getId()),
            account.getUpdatedAt()
        );
    }
}
