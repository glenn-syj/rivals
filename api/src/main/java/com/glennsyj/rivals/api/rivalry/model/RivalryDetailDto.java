package com.glennsyj.rivals.api.rivalry.model;

import java.time.LocalDateTime;
import java.util.List;

public record RivalryDetailDto(
        String rivalryId,
        List<ParticipantStatDto> leftStats,
        List<ParticipantStatDto> rightStats,
        LocalDateTime createdAt
) {
}
