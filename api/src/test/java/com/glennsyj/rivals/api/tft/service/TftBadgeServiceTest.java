package com.glennsyj.rivals.api.tft.service;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.tft.entity.achievement.AchievementType;
import com.glennsyj.rivals.api.tft.entity.achievement.TftBadgeProgress;
import com.glennsyj.rivals.api.tft.entity.achievement.TftMatchAchievement;
import com.glennsyj.rivals.api.tft.entity.match.TftMatch;
import com.glennsyj.rivals.api.tft.entity.match.TftMatchParticipant;
import com.glennsyj.rivals.api.tft.model.badge.TftBadgeBulkResponseDto;
import com.glennsyj.rivals.api.tft.model.badge.TftBadgeDto;
import com.glennsyj.rivals.api.tft.model.match.TftMatchUnit;
import com.glennsyj.rivals.api.tft.repository.TftBadgeProgressRepository;
import com.glennsyj.rivals.api.tft.repository.TftMatchAchievementRepository;
import com.glennsyj.rivals.api.riot.repository.RiotAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TftBadgeServiceTest {

    @Mock
    private TftMatchAchievementRepository achievementRepository;

    @Mock
    private TftBadgeProgressRepository badgeProgressRepository;

    @Mock
    private RiotAccountRepository riotAccountRepository;

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

    @Test
    void findAllBadges_ShouldReturnAllBadges_WhenBadgesDoesNotExistYet() {
        // Given
        RiotAccount account = mock(RiotAccount.class);
        when(account.getPuuid()).thenReturn("test-puuid");

        when(badgeProgressRepository.findByRiotAccount(account))
            .thenReturn(List.of());

        // 모든 BadgeType에 대해 countRecentAchievements가 호출될 때 0을 반환하도록 설정
        when(achievementRepository.countRecentAchievements(anyString(), anyString(), anyInt()))
            .thenReturn(0);

        // When
        List<TftBadgeDto> badges = tftBadgeService.findAllBadges(account);

        // Then
        // 모든 뱃지 타입이 생성되고 반환되는지 확인
        assertThat(badges).hasSize(TftBadgeProgress.BadgeType.values().length);
        assertThat(badges)
            .extracting(TftBadgeDto::badgeType)
            .containsExactlyInAnyOrderElementsOf(
                Arrays.stream(TftBadgeProgress.BadgeType.values())
                    .map(Enum::name)
                    .collect(Collectors.toList())
            );
        assertThat(badges)
            .extracting(TftBadgeDto::achievementType)
            .containsExactlyInAnyOrderElementsOf(
                Arrays.stream(TftBadgeProgress.BadgeType.values())
                    .map(badgeType -> badgeType.getAchievementType().name())
                    .collect(Collectors.toList())
            );
        assertThat(badges)
            .extracting(TftBadgeDto::isActive)
            .containsOnly(false); // 새로 생성된 뱃지는 초기에는 비활성화 상태여야 함

        // saveAll이 호출되었는지 확인
        verify(badgeProgressRepository).saveAll(anyList());
    }

    @Test
    void findBadge_ShouldReturnSpecificBadge() {
        // Given
        RiotAccount account = mock(RiotAccount.class);
        TftBadgeProgress.BadgeType badgeType = TftBadgeProgress.BadgeType.MVP;
        
        TftBadgeProgress mvpBadge = new TftBadgeProgress(account, badgeType);
        mvpBadge.updateProgress(6); // Required count is 6

        when(badgeProgressRepository.findByRiotAccountAndBadgeType(account, badgeType))
            .thenReturn(Optional.of(mvpBadge));

        // When
        Optional<TftBadgeDto> badge = tftBadgeService.findBadge(account, badgeType);

        // Then
        assertThat(badge).isPresent();
        assertThat(badge.get().badgeType()).isEqualTo("MVP");
        assertThat(badge.get().currentCount()).isEqualTo(6);
        assertThat(badge.get().isActive()).isTrue();
    }

    @Test
    void findBadge_ShouldReturnEmptyWhenBadgeNotFound() {
        // Given
        RiotAccount account = mock(RiotAccount.class);
        TftBadgeProgress.BadgeType badgeType = TftBadgeProgress.BadgeType.MVP;

        when(badgeProgressRepository.findByRiotAccountAndBadgeType(account, badgeType))
            .thenReturn(Optional.empty());

        // When
        Optional<TftBadgeDto> badge = tftBadgeService.findBadge(account, badgeType);

        // Then
        assertThat(badge).isEmpty();
    }

    @Test
    @DisplayName("findBadgesFromPuuids는 account가 존재하면 Badge를 반환한다")
    void findBadgesFromPuuids_ShouldReturnBadgesForExistingAccountsOnly() {
        // Given
        List<String> requestPuuids = List.of("existing-puuid1", "non-existing-puuid", "existing-puuid2");
        
        // 계정 2개만 존재하도록 모킹
        RiotAccount account1 = mock(RiotAccount.class);
        RiotAccount account2 = mock(RiotAccount.class);
        
        // Only 2 accounts are found
        List<RiotAccount> existingAccounts = List.of(account1, account2);
        when(riotAccountRepository.findAllByPuuidIn(requestPuuids)).thenReturn(existingAccounts);

        List<TftBadgeProgress> account1Badges = Arrays.stream(TftBadgeProgress.BadgeType.values())
            .map(type -> {
                TftBadgeProgress progress = new TftBadgeProgress(account1, type);
                progress.updateProgress(5);
                return progress;
            })
            .toList();

        List<TftBadgeProgress> account2Badges = Arrays.stream(TftBadgeProgress.BadgeType.values())
            .map(type -> {
                TftBadgeProgress progress = new TftBadgeProgress(account2, type);
                progress.updateProgress(3);
            })
            .toList();
        
        Map<String, List<TftBadgeProgress>> badgeProgressMap = Map.of(
            "existing-puuid1", account1Badges,
            "existing-puuid2", account2Badges
        );
        
        when(badgeProgressRepository.findMapByRiotAccountIn(existingAccounts)).thenReturn(badgeProgressMap);
        
        // When
        TftBadgeBulkResponseDto response = tftBadgeService.findBadgesFromPuuids(requestPuuids);
        
        // Then
        // 존재하는 계정의 puuid만 포함되어 있는지 확인
        assertThat(response.badgesOnPuuid())
            .containsOnlyKeys("existing-puuid1", "existing-puuid2")
            .doesNotContainKey("non-existing-puuid");
        
        // 각 계정이 모든 뱃지 타입을 가지고 있는지 확인
        List<String> expectedBadgeTypes = Arrays.stream(TftBadgeProgress.BadgeType.values())
            .map(Enum::name)
            .collect(Collectors.toList());
        
        // existing-puuid1의 뱃지 확인
        List<TftBadgeDto> puuid1Badges = response.badgesOnPuuid().get("existing-puuid1");
        assertThat(puuid1Badges)
            .hasSize(TftBadgeProgress.BadgeType.values().length)
            .extracting(TftBadgeDto::badgeType)
            .containsExactlyInAnyOrderElementsOf(expectedBadgeTypes);
        assertThat(puuid1Badges)
            .extracting(TftBadgeDto::currentCount)
            .containsOnly(5);

        // existing-puuid2의 뱃지 확인
        List<TftBadgeDto> puuid2Badges = response.badgesOnPuuid().get("existing-puuid2");
        assertThat(puuid2Badges)
            .hasSize(TftBadgeProgress.BadgeType.values().length)
            .extracting(TftBadgeDto::badgeType)
            .containsExactlyInAnyOrderElementsOf(expectedBadgeTypes);
        assertThat(puuid2Badges)
            .extracting(TftBadgeDto::currentCount)
            .containsOnly(3);
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