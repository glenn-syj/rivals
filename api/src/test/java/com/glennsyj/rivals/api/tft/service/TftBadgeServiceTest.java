package com.glennsyj.rivals.api.tft.service;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.tft.entity.achievement.AchievementType;
import com.glennsyj.rivals.api.tft.entity.achievement.TftBadgeProgress;
import com.glennsyj.rivals.api.tft.entity.achievement.TftMatchAchievement;
import com.glennsyj.rivals.api.tft.entity.match.TftMatch;
import com.glennsyj.rivals.api.tft.entity.match.TftMatchParticipant;
import com.glennsyj.rivals.api.tft.model.match.TftMatchUnit;
import com.glennsyj.rivals.api.tft.repository.TftBadgeProgressRepository;
import com.glennsyj.rivals.api.tft.repository.TftMatchAchievementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TftBadgeServiceTest {

    @Mock
    private TftMatchAchievementRepository achievementRepository;

    @Mock
    private TftBadgeProgressRepository badgeProgressRepository;

    @Captor
    private ArgumentCaptor<List<TftMatchAchievement>> achievementsCaptor;

    @InjectMocks
    private TftBadgeService tftBadgeService;

    @Test
    void processMatchAchievements_ShouldCreateAchievementsForMatch() {
        // Given
        TftMatch match = mock(TftMatch.class);
        
        // 1등 (최고 데미지, 최다 처치, 가장 비싼 스쿼드)
        TftMatchParticipant first = createParticipant(1, 3, 1500, List.of(
            new TftMatchUnit("TFT9_Ahri", List.of(), "Ahri", 4, 2),
            new TftMatchUnit("TFT9_Asol", List.of(), "ASol", 6, 1)
        ));
        
        // 2-4등 (상위 4등 업적)
        TftMatchParticipant second = createParticipant(2, 2, 1200, List.of(
            new TftMatchUnit("TFT9_Annie", List.of(), "Annie", 1, 1)
        ));
        TftMatchParticipant third = createParticipant(3, 1, 1000, List.of(
            new TftMatchUnit("TFT9_Karma", List.of(), "Karma", 2, 1)
        ));
        TftMatchParticipant fourth = createParticipant(4, 1, 800, List.of(
            new TftMatchUnit("TFT9_Lux", List.of(), "Lux", 3, 1)
        ));
        
        // 5-8등
        TftMatchParticipant fifth = createParticipant(5, 0, 600, List.of(
            new TftMatchUnit("TFT9_Sona", List.of(), "Sona", 2, 1)
        ));
        TftMatchParticipant sixth = createParticipant(6, 0, 500, List.of(
            new TftMatchUnit("TFT9_Viego", List.of(), "Viego", 1, 1)
        ));
        TftMatchParticipant seventh = createParticipant(7, 0, 400, List.of(
            new TftMatchUnit("TFT9_Yasuo", List.of(), "Yasuo", 1, 1)
        ));
        TftMatchParticipant eighth = createParticipant(8, 0, 300, List.of(
            new TftMatchUnit("TFT9_Zed", List.of(), "Zed", 1, 1)
        ));

        when(match.getParticipants()).thenReturn(List.of(
            first, second, third, fourth, fifth, sixth, seventh, eighth
        ));

        // When
        tftBadgeService.processMatchAchievements(match);

        // Then
        verify(achievementRepository).saveAll(achievementsCaptor.capture());
        List<TftMatchAchievement> savedAchievements = achievementsCaptor.getValue();

        assertThat(savedAchievements).hasSize(8); // 총 8개의 업적이 저장되어야 함
        assertThat(savedAchievements)
            .extracting(TftMatchAchievement::getType)
            .containsExactlyInAnyOrder(
                AchievementType.MOST_EXPENSIVE_SQUAD,  // 가장 비싼 스쿼드 (1회)
                AchievementType.MOST_DAMAGE_DEALT,     // 최고 데미지 (1회)
                AchievementType.MOST_ELIMINATIONS,     // 최다 처치 (1회)
                AchievementType.FIRST_PLACE,           // 1등 (1회)
                AchievementType.TOP_FOUR,              // 상위 4등 (4회, 1-4등)
                AchievementType.TOP_FOUR,
                AchievementType.TOP_FOUR,
                AchievementType.TOP_FOUR
            );

        // 추가 검증: 1등이 최고 데미지, 최다 처치, 가장 비싼 스쿼드를 모두 달성했는지 확인
        assertThat(savedAchievements)
            .filteredOn(achievement -> achievement.getAchiever().equals(first))
            .extracting(TftMatchAchievement::getType)
            .containsExactlyInAnyOrder(
                AchievementType.MOST_EXPENSIVE_SQUAD,
                AchievementType.MOST_DAMAGE_DEALT,
                AchievementType.MOST_ELIMINATIONS,
                AchievementType.FIRST_PLACE,
                AchievementType.TOP_FOUR
            );
    }

    @Test
    void renewAccountBadges_ShouldUpdateBadgeProgress() {
        // Given
        RiotAccount account = mock(RiotAccount.class);
        when(account.getPuuid()).thenReturn("test-puuid");
        
        // MVP 뱃지에 대한 기존 진행도
        TftBadgeProgress existingMvpProgress = new TftBadgeProgress(account, TftBadgeProgress.BadgeType.MVP);
        
        // BadgeType별로 다른 응답 설정
        when(badgeProgressRepository.findByRiotAccountAndBadgeType(eq(account), eq(TftBadgeProgress.BadgeType.MVP)))
            .thenReturn(Optional.of(existingMvpProgress));
        when(badgeProgressRepository.findByRiotAccountAndBadgeType(eq(account), argThat(badgeType -> 
            badgeType != TftBadgeProgress.BadgeType.MVP)))
            .thenReturn(Optional.empty());

        // BadgeType별로 다른 업적 카운트 설정
        when(achievementRepository.countRecentAchievements(
            eq("test-puuid"),
            eq(AchievementType.FIRST_PLACE.toString()),
            eq(20)
        )).thenReturn(5);
        when(achievementRepository.countRecentAchievements(
            eq("test-puuid"),
            argThat(achievementType -> !achievementType.equals(AchievementType.FIRST_PLACE.toString())),
            eq(20)
        )).thenReturn(0);

        // When
        tftBadgeService.renewAccountBadges(account);

        // Then
        // 모든 뱃지 타입에 대해 save가 호출되었는지 확인
        verify(badgeProgressRepository, times(TftBadgeProgress.BadgeType.values().length))
            .save(any(TftBadgeProgress.class));
        
        // MVP 뱃지의 진행도가 정확히 업데이트되었는지 확인
        assertThat(existingMvpProgress.getAchievementCount()).isEqualTo(5);
    }

    private TftMatchParticipant createParticipant(int placement, int eliminations, int damage, List<TftMatchUnit> units) {
        TftMatchParticipant participant = mock(TftMatchParticipant.class);
        when(participant.getPlacement()).thenReturn(placement);
        when(participant.getPlayersEliminated()).thenReturn(eliminations);
        when(participant.getTotalDamageToPlayers()).thenReturn(damage);
        when(participant.getUnits()).thenReturn(units);
        return participant;
    }
} 