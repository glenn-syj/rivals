package com.glennsyj.rivals.api.tft.model.badge;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "TFT 뱃지 대량 요청 DTO")
public record TftBadgeBulkRequestDto(
    @Schema(description = "PUUID 목록")
    @NotEmpty(message = "PUUIDs cannot be empty")
    List<String> puuids
) {
}
