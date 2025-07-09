package com.glennsyj.rivals.api.tft.model.match;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "TFT 매치 유닛 정보 DTO")
public record TftMatchUnit(
        @Schema(description = "캐릭터 ID")
        String character_id,
        @Schema(description = "아이템 이름 목록")
        List<String> itemNames,
        @Schema(description = "유닛 이름")
        String name,
        @Schema(description = "유닛 희귀도")
        Integer rarity,
        @Schema(description = "유닛 티어")
        Integer tier
) {}
