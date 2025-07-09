package com.glennsyj.rivals.api.tft.model.badge;

import com.glennsyj.rivals.api.tft.entity.achievement.TftBadgeProgress;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "TFT 뱃지 정보 DTO")
public record TftBadgeDto(
    @Schema(description = "뱃지 유형")
    String badgeType,
    @Schema(description = "업적 유형")
    String achievementType,
    @Schema(description = "현재 진행도")
    int currentCount,
    @Schema(description = "필요 진행도")
    int requiredCount,
    @Schema(description = "활성화 여부")
    boolean isActive
) {
    public static TftBadgeDto from(TftBadgeProgress progress) {
        return new TftBadgeDto(
            progress.getBadgeType().name(),
            progress.getBadgeType().getAchievementType().name(),
            progress.getAchievementCount(),
            progress.getBadgeType().getRequiredCount(),
            progress.isActive()
        );
    }
} 