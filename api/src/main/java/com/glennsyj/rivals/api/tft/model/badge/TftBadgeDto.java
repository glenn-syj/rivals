package com.glennsyj.rivals.api.tft.model.badge;

import com.glennsyj.rivals.api.tft.entity.achievement.TftBadgeProgress;

public record TftBadgeDto(
    String badgeType,
    String achievementType,
    int currentCount,
    int requiredCount,
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