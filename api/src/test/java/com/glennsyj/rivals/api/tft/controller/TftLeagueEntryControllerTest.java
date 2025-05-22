package com.glennsyj.rivals.api.tft.controller;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.service.RiotAccountManager;
import com.glennsyj.rivals.api.tft.entity.TftLeagueEntry;
import com.glennsyj.rivals.api.tft.model.TftLeagueEntryResponse;
import com.glennsyj.rivals.api.tft.service.TftLeagueEntryManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TftLeagueEntryController.class)
@WithMockUser
public class TftLeagueEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TftLeagueEntryManager leagueEntryManager;

    @MockitoBean
    private RiotAccountManager riotAccountManager;

    private RiotAccount account;
    private Long accountId;
    private TftLeagueEntry entry;

    @BeforeEach
    void createMock() {
        // RiotAccount 모의 객체 생성
        account = mock(RiotAccount.class);
        when(account.getGameName()).thenReturn("Hide");
        when(account.getTagLine()).thenReturn("KR1");
        when(account.getPuuid()).thenReturn("puuid0");
        when(account.getId()).thenReturn(100L); // ID를 모의로 설정
        when(account.getFullGameName()).thenReturn("Hide#KR1");

        // TftLeagueEntry 모의 객체 생성
        entry = mock(TftLeagueEntry.class);
        when(entry.getAccount()).thenReturn(account);
        when(entry.getPuuid()).thenReturn("puuid0");
        when(entry.getLeagueId()).thenReturn("league-id");
        when(entry.getSummonerId()).thenReturn("test-summoner-id");
        when(entry.getQueueType()).thenReturn(TftLeagueEntry.QueueType.RANKED_TFT);
        when(entry.getTier()).thenReturn(TftLeagueEntry.Tier.DIAMOND);
        when(entry.getRank()).thenReturn(TftLeagueEntry.Rank.I);
        when(entry.getLeaguePoints()).thenReturn(10);
        when(entry.getWins()).thenReturn(5);
        when(entry.getLosses()).thenReturn(2);
        when(entry.isHotStreak()).thenReturn(true);
        when(entry.isVeteran()).thenReturn(false);
        when(entry.isFreshBlood()).thenReturn(false);
        when(entry.isInactive()).thenReturn(false);
        when(entry.getMiniSeries()).thenReturn(null);
    }

    @Test
    void 존재하는_유저_TFT_정보_불러오기_성공() throws Exception {
        // given
        when(riotAccountManager.findOrRegisterAccount(account.getGameName(), account.getTagLine()))
                .thenReturn(account);
        when(leagueEntryManager.findOrCreateEntry(anyLong()))
                .thenReturn(entry);
        // when & then
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/tft/entries/{encodedFullName}",
                                account.getFullGameName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rank").value(entry.getRank().toString()))
                .andExpect(jsonPath("$.tier").value(entry.getTier().toString()))
                .andExpect(jsonPath("$.leaguePoints").value(entry.getLeaguePoints()))
                .andExpect(jsonPath("$.wins").value(entry.getWins()))
                .andExpect(jsonPath("$.losses").value(entry.getLosses()))
                .andExpect(jsonPath("$.hotStreak").value(entry.isHotStreak()))
                .andDo(print());
    }

    @Test
    void 존재하는_유저_TFT_정보_없음() throws Exception {
        // given
        when(riotAccountManager.findOrRegisterAccount(account.getGameName(), account.getTagLine()))
                .thenReturn(account);
        when(leagueEntryManager.findOrCreateEntry(anyLong()))
                .thenThrow(IllegalStateException.class);
        // when & then
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/v1/tft/entries/{encodedFullName}",
                                account.getFullGameName()))
                .andExpect(status().isNotFound())
                .andDo(print());

    }
}
