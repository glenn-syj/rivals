package com.glennsyj.rivals.api.tft.model.badge;

import java.util.List;
import java.util.Map;

public record TftBadgeBulkResponseDto(
        Map<String, List<TftBadgeDto>> badgesOnPuuid
) {
}
