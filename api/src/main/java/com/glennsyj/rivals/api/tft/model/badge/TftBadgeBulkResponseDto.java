package com.glennsyj.rivals.api.tft.model.badge;

import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "TFT 뱃지 대량 응답 DTO")
public record TftBadgeBulkResponseDto(
        @Schema(description = "PUUID별 뱃지 목록")
        Map<String, List<TftBadgeDto>> badgesOnPuuid
) {
}
