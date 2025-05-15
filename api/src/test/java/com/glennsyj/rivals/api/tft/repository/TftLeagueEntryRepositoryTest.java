package com.glennsyj.rivals.api.tft.repository;

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
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
}
