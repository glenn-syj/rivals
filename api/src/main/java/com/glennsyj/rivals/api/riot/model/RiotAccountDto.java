package com.glennsyj.rivals.api.riot.model;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import java.time.LocalDateTime;

public record RiotAccountDto(
        String puuid,
        String gameName,
        String tagLine,
        String id,
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
