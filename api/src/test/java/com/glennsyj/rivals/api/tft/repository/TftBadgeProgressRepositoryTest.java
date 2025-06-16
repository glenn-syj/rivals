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
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
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

    @BeforeEach
    void setUp() {
        // 테스트용 RiotAccount 생성
        testAccount = new RiotAccount(
            "test-gameName",
            "KR1",
            "test-puuid"
        );
        riotAccountRepository.save(testAccount);

        // 여러 뱃지 진행도 생성
        createBadgeProgress(TftBadgeProgress.BadgeType.LUXURY, 6, true);
        createBadgeProgress(TftBadgeProgress.BadgeType.DAMAGE_DEALER, 3, false);
        createBadgeProgress(TftBadgeProgress.BadgeType.MVP, 7, true);
        createBadgeProgress(TftBadgeProgress.BadgeType.STEADY, 4, false);
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

    private void createBadgeProgress(
        TftBadgeProgress.BadgeType badgeType,
        int achievementCount,
        boolean isActive
    ) {
        TftBadgeProgress progress = new TftBadgeProgress(testAccount, badgeType);
        progress.updateProgress(achievementCount);
        badgeProgressRepository.save(progress);
    }
} 