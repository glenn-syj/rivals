package com.glennsyj.rivals.api.tft.model.match;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "TFT 매치 특성 정보 DTO")
public record TftMatchTrait(
        @Schema(description = "특성 이름")
        String name,
        @Schema(description = "특성 유닛 수")
        Integer num_units,
        @Schema(description = "특성 스타일")
        Integer style,
        @Schema(description = "현재 특성 티어")
        Integer tier_current,
        @Schema(description = "총 특성 티어")
        Integer tier_total
) {}
