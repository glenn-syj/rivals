package com.glennsyj.rivals.api.tft.repository;

import com.glennsyj.rivals.api.config.TestContainerConfig;
import com.glennsyj.rivals.api.tft.entity.achievement.AchievementType;
import com.glennsyj.rivals.api.tft.entity.achievement.TftMatchAchievement;
import com.glennsyj.rivals.api.tft.entity.match.TftMatch;
import com.glennsyj.rivals.api.tft.entity.match.TftMatchParticipant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainerConfig.class)
@Testcontainers
class TftMatchAchievementRepositoryTest {

    @Autowired
    private TftMatchRepository matchRepository;

    @Autowired
    private TftMatchAchievementRepository achievementRepository;

    @Autowired
    private EntityManager entityManager;

    private final String TEST_PUUID = "test-puuid";
    private final int TOTAL_MATCHES = 25;
    private final int RECENT_MATCHES = 20;

    @BeforeEach
    void setUp() {
        // 25개의 테스트 매치 생성
        for (int i = 0; i < TOTAL_MATCHES; i++) {
            TftMatch match = createTestMatch("match-" + i);
            
            // 매치에 참가자 추가
            TftMatchParticipant participant = createTestParticipant(TEST_PUUID);
            match.addParticipant(participant);
            
            // 매치 저장
            matchRepository.save(match);
            
            // 매치 업적 추가 (3번째마다 MOST_EXPENSIVE_SQUAD 업적 달성)
            if (i % 3 == 0) {
                TftMatchAchievement achievement = new TftMatchAchievement(
                    match,
                    AchievementType.MOST_EXPENSIVE_SQUAD,
                    participant,
                    100 // 스쿼드 가치
                );
                achievementRepository.save(achievement);
            }
            
            // 5번째마다 FIRST_PLACE 업적 달성
            if (i % 5 == 0) {
                TftMatchAchievement achievement = new TftMatchAchievement(
                    match,
                    AchievementType.FIRST_PLACE,
                    participant,
                    1 // 1등
                );
                achievementRepository.save(achievement);
            }
        }
        
        // 변경사항을 DB에 즉시 반영
        // 테스트 코드에서 아래의 네이티브 쿼리 이용을 위해서는 flush
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("최근 20경기 중 특정 업적 달성 횟수를 정확히 계산한다")
    void countRecentAchievements() {
        // Debug: 전체 매치 정보 출력
        List<TftMatch> allMatches = matchRepository.findAll();
        System.out.println("\n=== 전체 매치 목록 ===");
        allMatches.stream()
            .sorted((m1, m2) -> m2.getGameCreation().compareTo(m1.getGameCreation()))
            .forEach(match -> {
                System.out.printf("Match ID: %s, Creation: %d%n", 
                    match.getMatchId(), match.getGameCreation());
            });

        // Debug: 최근 20경기 매치 ID 출력
        List<TftMatch> recentMatches = matchRepository
            .findTop20ByParticipantsPuuidOrderByGameCreationDesc(TEST_PUUID);
        System.out.println("\n=== 최근 20경기 매치 ID ===");
        recentMatches.forEach(match -> 
            System.out.println("Match ID: " + match.getMatchId()));

        // Debug: 업적 정보 출력
        List<TftMatchAchievement> achievements = achievementRepository.findAll();
        Map<AchievementType, Long> achievementCounts = achievements.stream()
            .filter(a -> a.getAchiever().getPuuid().equals(TEST_PUUID))
            .filter(a -> recentMatches.contains(a.getMatch()))
            .collect(Collectors.groupingBy(
                TftMatchAchievement::getType,
                Collectors.counting()
            ));

        System.out.println("\n=== 업적 달성 현황 ===");
        achievementCounts.forEach((type, count) -> 
            System.out.printf("%s: %d회%n", type, count));

        // when
        int expensiveSquadCount = achievementRepository.countRecentAchievements(
            TEST_PUUID,
            AchievementType.MOST_EXPENSIVE_SQUAD.name(),
            RECENT_MATCHES
        );

        int firstPlaceCount = achievementRepository.countRecentAchievements(
            TEST_PUUID,
            AchievementType.FIRST_PLACE.name(),
            RECENT_MATCHES
        );

        // Debug: 실제 쿼리 결과 출력
        System.out.println("\n=== 쿼리 결과 ===");
        System.out.printf("MOST_EXPENSIVE_SQUAD count: %d%n", expensiveSquadCount);
        System.out.printf("FIRST_PLACE count: %d%n", firstPlaceCount);

        // then
        assertThat(expensiveSquadCount).isEqualTo(7);
        assertThat(firstPlaceCount).isEqualTo(4);
    }

    private TftMatch createTestMatch(String matchId) {
        return new TftMatch(
            matchId,
            "1.0",
            System.currentTimeMillis(),
            1L,
            System.currentTimeMillis(),
            10.0,
            "version",
            "variation",
            1,
            1,
            "type",
            "core",
            1,
            "result"
        );
    }

    private TftMatchParticipant createTestParticipant(String puuid) {
        return new TftMatchParticipant(
            puuid,
            100,
            8,
            1000,
            8,
            4,
            3,
            "TestPlayer",
            "TEST",
            20.0,
            1000,
            false,
            null,
            List.of(),
            List.of()
        );
    }
} 