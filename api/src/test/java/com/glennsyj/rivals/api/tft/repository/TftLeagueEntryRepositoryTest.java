package com.glennsyj.rivals.api.tft.repository;

import com.glennsyj.rivals.api.config.EntityTestUtil;
import com.glennsyj.rivals.api.config.TestContainerConfig;
import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.repository.RiotAccountRepository;
import com.glennsyj.rivals.api.tft.entity.TftLeagueEntry;
import com.glennsyj.rivals.api.tft.model.TftLeagueEntryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainerConfig.class)
@Testcontainers
class TftLeagueEntryRepositoryTest {

    @Autowired
    private TftLeagueEntryRepository tftLeagueEntryRepository;

    @Autowired
    private RiotAccountRepository riotAccountRepository;

    @Test
    @DisplayName("계정 ID로 TFT 리그 정보를 정확히 조회한다")
    void findByAccountId_ShouldReturnCorrectEntry() {
        // given
        RiotAccount account = new RiotAccount("testUser", "KR1", "test-puuid");
        account = riotAccountRepository.save(account);

        TftLeagueEntry entry = new TftLeagueEntry(account, createMockResponse());
        tftLeagueEntryRepository.save(entry);

        // when
        Optional<TftLeagueEntry> found = tftLeagueEntryRepository.findByAccount_Id(account.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getAccount().getId()).isEqualTo(account.getId());
    }

    @Test
    @DisplayName("존재하지 않는 계정 ID로 조회시 빈 Optional을 반환한다")
    void findByAccountId_ShouldReturnEmptyForNonExistentId() {
        // given
        Long nonExistentId = 999L;

        // when
        Optional<TftLeagueEntry> found = tftLeagueEntryRepository.findByAccount_Id(nonExistentId);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("QueueType 별로 엔트리를 저장하면 가장 최근 엔트리를 불러온다.")
    void findLatestEntriesForEachQueueTypeByAccountId_ShouldReturnLatestEntryForEachQueueType() {
        // given
        RiotAccount account = new RiotAccount("gameName", "tagLine", "puuid");
        account = riotAccountRepository.save(account);
        LocalDateTime now = LocalDateTime.now();
        // 각 QueueType별로 2개의 엔트리 저장 (이전 데이터 + 최신 데이터)
        TftLeagueEntry oldRankedEntry = createEntry(account, TftLeagueEntry.QueueType.RANKED_TFT, now.minusDays(1));
        TftLeagueEntry latestRankedEntry = createEntry(account, TftLeagueEntry.QueueType.RANKED_TFT, now);

        TftLeagueEntry oldTurboEntry = createEntry(account, TftLeagueEntry.QueueType.RANKED_TFT_TURBO, now.minusDays(1));
        TftLeagueEntry latestTurboEntry = createEntry(account, TftLeagueEntry.QueueType.RANKED_TFT_TURBO, now);

        tftLeagueEntryRepository.saveAll(List.of(oldRankedEntry, latestRankedEntry, oldTurboEntry, latestTurboEntry));

        // when
        List<TftLeagueEntry> result = tftLeagueEntryRepository.findLatestEntriesForEachQueueTypeByAccountId(account.getId());

        // then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.containsAll(List.of(latestRankedEntry, latestTurboEntry)))
                .isEqualTo(true);
    }


    private TftLeagueEntryResponse createMockResponse() {
        return new TftLeagueEntryResponse(
                "test-puuid",
                "test-league-id",
                "test-summoner-id",
                "RANKED_TFT",
                "DIAMOND",
                "I",
                100,
                10,
                5,
                false,
                false,
                false,
                false,
                null
        );
    }

    private TftLeagueEntry createEntry(RiotAccount account, TftLeagueEntry.QueueType queueType, LocalDateTime updatedAt) {
        TftLeagueEntryResponse response = new TftLeagueEntryResponse(
                account.getPuuid(),
                "test-leagueId",
                "test-summonerId",
                queueType.name(),
                TftLeagueEntry.Tier.GOLD.name(),
                TftLeagueEntry.Rank.I.name(),
                100,  // leaguePoints
                10,   // wins
                5,    // losses
                false, // hotStreak
                false, // veteran
                false, // freshBlood
                false, // inactive
                null   // miniSeries
        );

        TftLeagueEntry entry = new TftLeagueEntry(account, response);

        // updatedAt 설정을 위한 리플렉션 사용
        ReflectionTestUtils.setField(entry, "updatedAt", updatedAt);

        return entry;
    }
}
