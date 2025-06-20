package com.glennsyj.rivals.api.tft.service;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.tft.entity.achievement.AchievementType;
import com.glennsyj.rivals.api.tft.entity.achievement.TftBadgeProgress;
import com.glennsyj.rivals.api.tft.entity.achievement.TftMatchAchievement;
import com.glennsyj.rivals.api.tft.entity.match.TftMatch;
import com.glennsyj.rivals.api.tft.entity.match.TftMatchParticipant;
import com.glennsyj.rivals.api.tft.model.badge.TftBadgeDto;
import com.glennsyj.rivals.api.tft.repository.TftBadgeProgressRepository;
import com.glennsyj.rivals.api.tft.repository.TftMatchAchievementRepository;
import com.glennsyj.rivals.api.riot.service.RiotAccountManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TftBadgeService {
    private static final int RECENT_MATCHES_COUNT = 20;

    private final TftMatchAchievementRepository achievementRepository;
    private final TftBadgeProgressRepository badgeProgressRepository;
    private final RiotAccountManager riotAccountManager;

    public TftBadgeService(
            TftMatchAchievementRepository achievementRepository,
            TftBadgeProgressRepository badgeProgressRepository,
            RiotAccountManager riotAccountManager) {
        this.achievementRepository = achievementRepository;
        this.badgeProgressRepository = badgeProgressRepository;
        this.riotAccountManager = riotAccountManager;
    }

    @Transactional
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
    public List<TftBadgeDto> renewAccountBadges(RiotAccount account) {
        // 각 뱃지 타입별로 진행도 업데이트
        for (TftBadgeProgress.BadgeType badgeType : TftBadgeProgress.BadgeType.values()) {
            updateBadgeProgress(account, badgeType);
        }

        return initializeOrGetBadges(account);
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

    /**
     * 등록된 소환사의 뱃지를 조회하거나 초기화합니다.
     * 등록되지 않은 소환사의 경우 빈 리스트를 반환합니다.
     */
    @Transactional
    public List<TftBadgeDto> findAllBadges(String gameName, String tagLine) {
        return riotAccountManager.findByGameNameAndTagLine(gameName, tagLine)
            .map(this::initializeOrGetBadges)
            .orElse(Collections.emptyList());
    }

    /**
     * 등록된 소환사의 뱃지를 조회하거나 초기화합니다.
     */
    @Transactional
    public List<TftBadgeDto> findAllBadges(RiotAccount account) {
        return initializeOrGetBadges(account);
    }

    /**
     * 등록된 소환사의 특정 뱃지를 조회합니다.
     */
    @Transactional(readOnly = true)
    public Optional<TftBadgeDto> findBadge(RiotAccount account, TftBadgeProgress.BadgeType badgeType) {
        return badgeProgressRepository.findByRiotAccountAndBadgeType(account, badgeType)
            .map(TftBadgeDto::from);
    }

    /**
     * 계정의 뱃지를 초기화하거나 가져옵니다.
     * 뱃지가 없는 경우 새로 생성하고, 있는 경우 기존 뱃지를 업데이트합니다.
     */
    private List<TftBadgeDto> initializeOrGetBadges(RiotAccount account) {
        List<TftBadgeProgress> existingBadges = badgeProgressRepository.findByRiotAccount(account);
        
        if (existingBadges.isEmpty()) {
            // 뱃지가 없는 경우, 모든 뱃지 타입에 대해 새로운 진행도 생성
            List<TftBadgeProgress> newBadges = Arrays.stream(TftBadgeProgress.BadgeType.values())
                .map(badgeType -> {
                    TftBadgeProgress progress = new TftBadgeProgress(account, badgeType);
                    // 최근 N경기의 업적 달성 횟수 조회 및 설정
                    int recentCount = achievementRepository.countRecentAchievements(
                        account.getPuuid(),
                        badgeType.getAchievementType().toString(),
                        RECENT_MATCHES_COUNT
                    );
                    progress.updateProgress(recentCount);
                    return progress;
                })
                .collect(Collectors.toList());
            
            // 새로운 뱃지들 저장
            badgeProgressRepository.saveAll(newBadges);
            return newBadges.stream()
                .map(TftBadgeDto::from)
                .collect(Collectors.toList());
        } else {
            // 기존 뱃지가 있는 경우, 모든 뱃지 업데이트
            renewAccountBadges(account);
            return existingBadges.stream()
                .map(TftBadgeDto::from)
                .collect(Collectors.toList());
        }
    }

    /**
     * 계정의 뱃지를 가져옵니다.
     */
    private List<TftBadgeDto> getBadges(RiotAccount account) {
        List<TftBadgeProgress> existingBadges = badgeProgressRepository.findByRiotAccount(account);

        return existingBadges.stream()
                .map(TftBadgeDto::from)
                .collect(Collectors.toList());

    }
} 