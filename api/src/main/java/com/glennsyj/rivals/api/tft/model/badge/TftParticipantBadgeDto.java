package com.glennsyj.rivals.api.tft.model.badge;

public record TftParticipantBadgeDto(
    PlayerIdentifierDto player,
    TftBadgeDto badge
) {
}
