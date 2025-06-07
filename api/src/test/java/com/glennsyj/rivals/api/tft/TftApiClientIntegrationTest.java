package com.glennsyj.rivals.api.tft;

import com.glennsyj.rivals.api.riot.RiotAccountClient;
import com.glennsyj.rivals.api.tft.model.entry.TftLeagueEntryResponse;
import com.glennsyj.rivals.api.tft.model.match.TftMatchResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Objects;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class TftApiClientIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(TftApiClientIntegrationTest.class);

    @Autowired
    private TftApiClient tftApiClient;

    @Autowired
    private RiotAccountClient riotAccountClient;

    @Test
    void TFT_랭크_엔트리_실제API호출_테스트() throws InterruptedException {
        // given
        String gameName = "승상싱";
        String tagLine = "KR1";

        String puuid = riotAccountClient.getAccountInfo(gameName, tagLine).puuid();

        // when
        List<TftLeagueEntryResponse> response = tftApiClient.getLeagueEntries(puuid);

        log.debug("puuid returned: {}", puuid);
        log.debug(response.toString());
        log.debug(String.valueOf(response.getClass()));

        TftLeagueEntryResponse entry0 = response.get(0);
        // then
        assertNotNull(response);
        assertThat(Objects.equals(entry0.puuid(), puuid));
    }

    @Test
    void TFT_매치_ID_실제API호출_테스트() throws InterruptedException {
        // given
        String gameName = "승상싱";
        String tagLine = "KR1";

        String puuid = riotAccountClient.getAccountInfo(gameName, tagLine).puuid();

        // when
        List<String> matchIds = tftApiClient.getMatchIdsFromPuuid(puuid);

        log.debug("puuid returned: {}", puuid);
        log.debug("Match IDs: {}", matchIds);

        // then
        assertNotNull(matchIds);
        // 매치 ID 리스트가 비어있지 않음을 확인 -> 그러나 실제 로직에서는 비어있는 경우도 고려
        assertThat(matchIds).isNotEmpty();
    }

    @Test
    void TFT_매치_ID_기반_매치_정보_실제API호출_테스트() throws InterruptedException {
        // given
        String gameName = "승상싱";
        String tagLine = "KR1";

        String puuid = riotAccountClient.getAccountInfo(gameName, tagLine).puuid();
        List<String> matchIds = tftApiClient.getMatchIdsFromPuuid(puuid);

        // 매치 ID가 존재하는지 확인
        // 존재하지 않으면 다른 유저 정보 이용 시도해야 함
        assertNotNull(matchIds);
        assertThat(matchIds).isNotEmpty();

        String matchId = matchIds.get(0); // 첫 번째 매치 ID를 사용

        // when
        TftMatchResponse matchResponse = tftApiClient.getMatchResponseFromMatchId(matchId);

        // then
        assertNotNull(matchResponse);
        log.debug("Match Response: {}", matchResponse);
    }
}
