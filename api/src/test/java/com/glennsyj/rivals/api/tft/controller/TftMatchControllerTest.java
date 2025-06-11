package com.glennsyj.rivals.api.tft.controller;

import com.glennsyj.rivals.api.config.EntityTestUtil;
import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.service.RiotAccountManager;
import com.glennsyj.rivals.api.tft.entity.match.TftMatch;
import com.glennsyj.rivals.api.tft.entity.match.TftMatchParticipant;
import com.glennsyj.rivals.api.tft.model.match.TftRecentMatchDto;
import com.glennsyj.rivals.api.tft.service.TftMatchManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TftMatchController.class)
@WithMockUser
class TftMatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TftMatchManager tftMatchManager;

    @MockitoBean
    private RiotAccountManager riotAccountManager;

    private RiotAccount mockRiotAccount;
    private List<TftMatch> mockMatches;

    @BeforeEach
    void setUp() {
        // Mock RiotAccount 설정
        mockRiotAccount = createMockRiotAccount();

        // Mock TftMatch 설정
        mockMatches = createMockTftMatches();

        createMatchParticipant(mockMatches.get(0));
    }

    private RiotAccount createMockRiotAccount() {
        RiotAccount account = new RiotAccount(
            "testGame",
            "testTag",
            "test-puuid"
        );
        EntityTestUtil.setId(account, 1L);
        return account;
    }

    private List<TftMatch> createMockTftMatches() {
        List<TftMatch> matches = new ArrayList<>();
        TftMatch match = new TftMatch(
                "TEST_MATCH_ID",
                "test-version",
                1234567890L,  // gameCreation
                1L,           // gameId
                1234567890L,  // gameDateTime
                1800.0,       // gameLength
                "test-game-version",
                "test-variation",
                1,            // mapId
                1100,         // queueId
                "standard",   // tftGameType
                "TFTSet9_2",  // tftSetCoreName
                9,            // tftSetNumber
                "complete"    // endOfGameResult
        );
        EntityTestUtil.setId(match, 1L);
        matches.add(match);
        return matches;
    }

    private TftMatchParticipant createMatchParticipant(TftMatch match) {
        TftMatchParticipant tftMatchParticipant = new TftMatchParticipant(
                "test-puuid",           // puuid
                50,                     // goldLeft
                5,                      // lastRound
                10,                     // missionsPlayerScore2
                3,                      // level
                1,                      // placement
                0,                      // playersEliminated
                "testGame",            // riotIdGameName
                "testTag",             // riotIdTagline
                120.0,                 // timeEliminated
                2000,                  // totalDamageToPlayers
                true,                  // win
                null,                  // companion (null or create a mock if needed)
                new ArrayList<>(),     // traits (empty list or add mock traits)
                new ArrayList<>()       // units (empty list or add mock units)
        );
        EntityTestUtil.setFieldWithValue(tftMatchParticipant, "match", match);
        match.addParticipant(tftMatchParticipant);

        return tftMatchParticipant;
    }

    @Test
    @DisplayName("GET /api/v1/tft/matches/{gameName}/{tagLine} - 성공")
    void findOrCreateRecentTftMatches_Success() throws Exception {
        // Given
        String gameName = "testGame";
        String tagLine = "testTag";

        when(riotAccountManager.findOrRegisterAccount(gameName, tagLine))
                .thenReturn(mockRiotAccount);
        when(tftMatchManager.findOrCreateRecentTftMatches(
                mockRiotAccount.getId(), 
                mockRiotAccount.getPuuid()))
                .thenReturn(mockMatches);

        // When & Then
        mockMvc.perform(get("/api/v1/tft/matches/{gameName}/{tagLine}", gameName, tagLine))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].matchId").value("TEST_MATCH_ID"))
                .andExpect(jsonPath("$[0].gameCreation").value(1234567890))
                .andExpect(jsonPath("$[0].gameLength").value(1800.0))
                .andExpect(jsonPath("$[0].level").value(3))
                .andExpect(jsonPath("$[0].queueType").value("standard"))
                .andExpect(jsonPath("$[0].traits").isArray())
                .andExpect(jsonPath("$[0].units").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/tft/matches/{gameName}/{tagLine} - 실패 (매치 조회 실패)")
    void findOrCreateRecentTftMatches_MatchLookupFailure() throws Exception {
        // Given
        String gameName = "testGame";
        String tagLine = "testTag";

        when(riotAccountManager.findOrRegisterAccount(gameName, tagLine))
                .thenReturn(mockRiotAccount);
        when(tftMatchManager.findOrCreateRecentTftMatches(
                mockRiotAccount.getId(), 
                mockRiotAccount.getPuuid()))
                .thenThrow(new IllegalStateException("계정 갱신을 먼저 진행해주세요."));

        // When & Then
        mockMvc.perform(get("/api/v1/tft/matches/{gameName}/{tagLine}", gameName, tagLine))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/tft/matches/{gameName}/{tagLine} - 실패 (계정 조회 실패)")
    void findOrCreateRecentTftMatches_AccountLookupFailure() throws Exception {
        // Given
        String gameName = "testGame";
        String tagLine = "testTag";

        when(riotAccountManager.findOrRegisterAccount(anyString(), anyString()))
                .thenThrow(new IllegalStateException("계정을 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/api/v1/tft/matches/{gameName}/{tagLine}", gameName, tagLine))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").doesNotExist());
    }
}
