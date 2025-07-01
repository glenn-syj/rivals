package com.glennsyj.rivals.api.tft.repository;

import com.glennsyj.rivals.api.config.TestContainerConfig;
import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.repository.RiotAccountRepository;
import com.glennsyj.rivals.api.tft.entity.achievement.TftBadgeProgress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainerConfig.class)
@Testcontainers
class TftBadgeProgressRepositoryTest {

    @Autowired
    private TftBadgeProgressRepository badgeProgressRepository;

    @Autowired
    private RiotAccountRepository riotAccountRepository;

    private RiotAccount testAccount;
    private RiotAccount testAccount2;

    @BeforeEach
    void setUp() {
        // 테스트용 RiotAccount 생성
        testAccount = new RiotAccount(
            "test-gameName",
            "KR1",
            "test-puuid"
        );
        testAccount2 = new RiotAccount(
                "test-gameName2",
                "KR2",
                "test-puuid2"
        );

        riotAccountRepository.saveAll(List.of(testAccount, testAccount2));

        // 여러 뱃지 진행도 생성
        createBadgeProgress(TftBadgeProgress.BadgeType.LUXURY, 6, true, testAccount);
        createBadgeProgress(TftBadgeProgress.BadgeType.DAMAGE_DEALER, 3, false, testAccount);
        createBadgeProgress(TftBadgeProgress.BadgeType.MVP, 7, true, testAccount);
        createBadgeProgress(TftBadgeProgress.BadgeType.STEADY, 4, false, testAccount);

        createBadgeProgress(TftBadgeProgress.BadgeType.LUXURY, 6, true, testAccount2);
        createBadgeProgress(TftBadgeProgress.BadgeType.DAMAGE_DEALER, 3, false, testAccount2);
        createBadgeProgress(TftBadgeProgress.BadgeType.MVP, 7, true, testAccount2);
        createBadgeProgress(TftBadgeProgress.BadgeType.STEADY, 4, false, testAccount2);
    }

    @Test
    @DisplayName("계정과 뱃지 타입으로 진행도를 정확히 조회한다")
    void findByRiotAccountAndBadgeType() {
        // when
        Optional<TftBadgeProgress> luxuryProgress = badgeProgressRepository
            .findByRiotAccountAndBadgeType(testAccount, TftBadgeProgress.BadgeType.LUXURY);
        
        Optional<TftBadgeProgress> damageProgress = badgeProgressRepository
            .findByRiotAccountAndBadgeType(testAccount, TftBadgeProgress.BadgeType.DAMAGE_DEALER);

        // then
        assertThat(luxuryProgress).isPresent();
        assertThat(luxuryProgress.get().getAchievementCount()).isEqualTo(6);
        assertThat(luxuryProgress.get().isActive()).isTrue();

        assertThat(damageProgress).isPresent();
        assertThat(damageProgress.get().getAchievementCount()).isEqualTo(3);
        assertThat(damageProgress.get().isActive()).isFalse();
    }

    @Test
    @DisplayName("계정의 활성화된 뱃지 목록만 조회한다")
    void findByRiotAccountAndIsActiveTrue() {
        // when
        List<TftBadgeProgress> activeBadges = badgeProgressRepository
            .findByRiotAccountAndIsActiveTrue(testAccount);

        // then
        assertThat(activeBadges).hasSize(2);
        assertThat(activeBadges)
            .extracting(TftBadgeProgress::getBadgeType)
            .containsExactlyInAnyOrder(
                TftBadgeProgress.BadgeType.LUXURY,
                TftBadgeProgress.BadgeType.MVP
            );
    }

    @Test
    @DisplayName("여러 계정에 대해서 BadgeProgress를 Map 형태로 조회한다")
    void findMapByRiotAccountIn() {

        // when
        Map<Long, List<TftBadgeProgress>> badgeMap = badgeProgressRepository
                .findMapByRiotAccountIn(List.of(testAccount, testAccount2));

        // then
        assertThat(badgeMap).hasSize(2);
        assertThat(badgeMap.get(testAccount.getId())).isNotNull();
        assertThat(badgeMap.get(testAccount2.getId())).isNotNull();
    }

    private void createBadgeProgress(
        TftBadgeProgress.BadgeType badgeType,
        int achievementCount,
        boolean isActive,
        RiotAccount account
    ) {
        TftBadgeProgress progress = new TftBadgeProgress(account, badgeType);
        progress.updateProgress(achievementCount);
        badgeProgressRepository.save(progress);
    }
} 