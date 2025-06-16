package com.glennsyj.rivals.api.tft.service;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.tft.entity.achievement.AchievementType;
import com.glennsyj.rivals.api.tft.entity.achievement.TftBadgeProgress;
import com.glennsyj.rivals.api.tft.entity.achievement.TftMatchAchievement;
import com.glennsyj.rivals.api.tft.entity.match.TftMatch;
import com.glennsyj.rivals.api.tft.entity.match.TftMatchParticipant;
import com.glennsyj.rivals.api.tft.repository.TftBadgeProgressRepository;
import com.glennsyj.rivals.api.tft.repository.TftMatchAchievementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class TftBadgeService {
    private static final int RECENT_MATCHES_COUNT = 20;

    private final TftMatchAchievementRepository achievementRepository;
    private final TftBadgeProgressRepository badgeProgressRepository;

    public TftBadgeService(
            TftMatchAchievementRepository achievementRepository,
            TftBadgeProgressRepository badgeProgressRepository) {
        this.achievementRepository = achievementRepository;
        this.badgeProgressRepository = badgeProgressRepository;
    }

    public void processMatchAchievements(TftMatch match) {
        // 1. 매치 내 업적 계산 및 저장
        calculateAndSaveAchievements(match);
    }

    private void calculateAndSaveAchievements(TftMatch match) {
        List<TftMatchParticipant> participants = match.getParticipants();
        List<TftMatchAchievement> achievements = new ArrayList<>();

        // 가장 비싼 스쿼드 찾기
        TftMatchParticipant mostExpensive = findMostExpensiveSquad(participants);
        achievements.add(createAchievement(match, AchievementType.MOST_EXPENSIVE_SQUAD, 
            mostExpensive, calculateSquadValue(mostExpensive)));

        // 최대 데미지
        TftMatchParticipant mostDamage = findHighestDamageDealer(participants);
        achievements.add(createAchievement(match, AchievementType.MOST_DAMAGE_DEALT, 
            mostDamage, mostDamage.getTotalDamageToPlayers()));

        // 최다 처치
        TftMatchParticipant mostEliminations = findMostEliminations(participants);
        achievements.add(createAchievement(match, AchievementType.MOST_ELIMINATIONS, 
            mostEliminations, mostEliminations.getPlayersEliminated()));

        // 1등
        participants.stream()
            .filter(p -> p.getPlacement() == 1)
            .findFirst()
            .ifPresent(winner -> achievements.add(
                createAchievement(match, AchievementType.FIRST_PLACE, winner, 1)));

        // 상위 4등
        participants.stream()
            .filter(p -> p.getPlacement() <= 4)
            .map(p -> createAchievement(match, AchievementType.TOP_FOUR, p, p.getPlacement()))
            .forEach(achievements::add);

        // 한 번에 모든 업적 저장
        achievementRepository.saveAll(achievements);
    }

    private TftMatchAchievement createAchievement(TftMatch match, AchievementType type, 
        TftMatchParticipant achiever, Integer value) {
        return new TftMatchAchievement(match, type, achiever, value);
    }

    @Transactional
    public void renewAccountBadges(RiotAccount account) {
        // 각 뱃지 타입별로 진행도 업데이트
        for (TftBadgeProgress.BadgeType badgeType : TftBadgeProgress.BadgeType.values()) {
            updateBadgeProgress(account, badgeType);
        }
    }

    private void updateBadgeProgress(RiotAccount account, TftBadgeProgress.BadgeType badgeType) {
        // 진행도 조회 또는 생성
        TftBadgeProgress progress = badgeProgressRepository
            .findByRiotAccountAndBadgeType(account, badgeType)
            .orElseGet(() -> new TftBadgeProgress(account, badgeType));

        // 최근 N경기의 업적 달성 횟수 조회
        int recentCount = achievementRepository.countRecentAchievements(
            account.getPuuid(),
            badgeType.getAchievementType().toString(),
            RECENT_MATCHES_COUNT
        );

        // 진행도 업데이트
        progress.updateProgress(recentCount);
        badgeProgressRepository.save(progress);
    }

    // 업적 계산 헬퍼 메서드들
    private TftMatchParticipant findMostExpensiveSquad(List<TftMatchParticipant> participants) {
        return Collections.max(participants, Comparator.comparing(this::calculateSquadValue));
    }

    private int calculateSquadValue(TftMatchParticipant participant) {
        return participant.getUnits().stream()
            .mapToInt(unit -> {
                int basePrice = switch (unit.rarity()) {
                    case 0 -> 1;  // 1원 유닛
                    case 1 -> 2;  // 2원 유닛
                    case 2 -> 3;  // 3원 유닛
                    case 4 -> 4;  // 4원 유닛
                    case 6 -> 5;  // 5원 유닛
                    default -> 0;
                };
                return basePrice * (int) Math.pow(3, unit.tier() - 1);
            })
            .sum();
    }

    private TftMatchParticipant findHighestDamageDealer(List<TftMatchParticipant> participants) {
        return Collections.max(participants, 
            Comparator.comparing(TftMatchParticipant::getTotalDamageToPlayers));
    }

    private TftMatchParticipant findMostEliminations(List<TftMatchParticipant> participants) {
        return Collections.max(participants, 
            Comparator.comparing(TftMatchParticipant::getPlayersEliminated));
    }
} 