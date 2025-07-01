package com.glennsyj.rivals.api.tft.model.badge;

import java.util.List;

public record TftBadgeBulkResponseDto(
    List<TftParticipantBadgeDto> participantsWithBadge
) {
}
