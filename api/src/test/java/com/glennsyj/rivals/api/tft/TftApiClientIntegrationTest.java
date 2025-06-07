package com.glennsyj.rivals.api.tft;

import com.glennsyj.rivals.api.riot.RiotAccountClient;
import com.glennsyj.rivals.api.tft.model.entry.TftLeagueEntryResponse;
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
}
