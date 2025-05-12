package com.glennsyj.rivals.api.riot;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class RiotApiClientIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(RiotApiClientIntegrationTest.class);

    @Autowired
    private RiotApiClient riotApiClient;

    @Test
    void getPUUID_실제API호출_테스트() {
        // given
        String gameName = "hide on bush";
        String tagLine = "KR1";

        // when
        String puuid = riotApiClient.getPUUID(gameName, tagLine);

        log.debug("puuid returned: {}", puuid);
        // then
        assertNotNull(puuid);
    }
}
