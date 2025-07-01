package com.glennsyj.rivals.api.tft.model.badge;

import java.util.List;

public record TftParticipantBadgeDto(
    String puuid,
    List<TftBadgeDto> badges
) {
}
