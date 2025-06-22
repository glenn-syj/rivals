package com.glennsyj.rivals.api.tft.controller;

import com.glennsyj.rivals.api.config.EntityTestUtil;
import com.glennsyj.rivals.api.tft.entity.match.TftMatch;
import com.glennsyj.rivals.api.tft.entity.match.TftMatchParticipant;
import com.glennsyj.rivals.api.tft.facade.TftFacade;
import com.glennsyj.rivals.api.tft.model.match.TftMatchParticipantDto;
import com.glennsyj.rivals.api.tft.model.match.TftMatchTrait;
import com.glennsyj.rivals.api.tft.model.match.TftMatchUnit;
import com.glennsyj.rivals.api.tft.model.match.TftRecentMatchDto;
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
    private TftFacade tftFacade;

    private List<TftRecentMatchDto> mockMatchDtos;

    @BeforeEach
    void setUp() {
        // Mock TftRecentMatchDto 설정
        mockMatchDtos = createMockTftRecentMatchDtos();
    }

    private List<TftRecentMatchDto> createMockTftRecentMatchDtos() {
        List<TftRecentMatchDto> dtos = new ArrayList<>();
        List<TftMatchParticipantDto> participants = new ArrayList<>();
        participants.add(new TftMatchParticipantDto(
            "test-puuid",
            3,                  // level
            1,                  // placement
            2000,               // totalDamageToPlayers
            "testGame",         // riotIdGameName
            "testTag",          // riotIdTagline
            new ArrayList<>(),  // traits
            new ArrayList<>()   // units
        ));

        TftRecentMatchDto dto = new TftRecentMatchDto(
            "1",                // id
            "TEST_MATCH_ID",    // matchId
            1234567890L,        // gameCreation
            1800.0,             // gameLength
            3,                  // level
            1,                  // placement
            "standard",         // queueType
            new ArrayList<>(),  // traits
            new ArrayList<>(),  // units
            participants        // participants
        );
        dtos.add(dto);
        return dtos;
    }

    @Test
    @DisplayName("GET /api/v1/tft/matches/{gameName}/{tagLine} - 성공")
    void getRecentMatches_Success() throws Exception {
        // Given
        String gameName = "testGame";
        String tagLine = "testTag";

        when(tftFacade.findAndProcessMatches(gameName, tagLine))
                .thenReturn(mockMatchDtos);

        // When & Then
        mockMvc.perform(get("/api/v1/tft/matches/{gameName}/{tagLine}", gameName, tagLine))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].matchId").value("TEST_MATCH_ID"))
                .andExpect(jsonPath("$[0].gameCreation").value(1234567890))
                .andExpect(jsonPath("$[0].gameLength").value(1800.0))
                .andExpect(jsonPath("$[0].level").value(3))
                .andExpect(jsonPath("$[0].placement").value(1))
                .andExpect(jsonPath("$[0].queueType").value("standard"))
                .andExpect(jsonPath("$[0].traits").isArray())
                .andExpect(jsonPath("$[0].units").isArray())
                .andExpect(jsonPath("$[0].participants").isArray())
                .andExpect(jsonPath("$[0].participants[0].level").value(3))
                .andExpect(jsonPath("$[0].participants[0].placement").value(1))
                .andExpect(jsonPath("$[0].participants[0].totalDamageToPlayers").value(2000))
                .andExpect(jsonPath("$[0].participants[0].riotIdGameName").value("testGame"))
                .andExpect(jsonPath("$[0].participants[0].riotIdTagline").value("testTag"));
    }

    @Test
    @DisplayName("GET /api/v1/tft/matches/{gameName}/{tagLine} - 실패 (매치 조회 실패)")
    void getRecentMatches_MatchLookupFailure() throws Exception {
        // Given
        String gameName = "testGame";
        String tagLine = "testTag";

        when(tftFacade.findAndProcessMatches(gameName, tagLine))
                .thenThrow(new IllegalStateException("계정 갱신을 먼저 진행해주세요."));

        // When & Then
        mockMvc.perform(get("/api/v1/tft/matches/{gameName}/{tagLine}", gameName, tagLine))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/tft/matches/{gameName}/{tagLine} - 실패 (계정 조회 실패)")
    void getRecentMatches_AccountLookupFailure() throws Exception {
        // Given
        String gameName = "testGame";
        String tagLine = "testTag";

        when(tftFacade.findAndProcessMatches(gameName, tagLine))
                .thenThrow(new IllegalStateException("계정을 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/api/v1/tft/matches/{gameName}/{tagLine}", gameName, tagLine))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").doesNotExist());
    }
}
