package com.glennsyj.rivals.api.tft.controller;

import com.glennsyj.rivals.api.common.config.SecurityConfig;
import com.glennsyj.rivals.api.tft.facade.TftFacade;
import com.glennsyj.rivals.api.tft.facade.exception.TftRenewException;
import com.glennsyj.rivals.api.tft.model.badge.TftBadgeDto;
import com.glennsyj.rivals.api.tft.model.entry.TftStatusDto;
import com.glennsyj.rivals.api.tft.model.match.TftRecentMatchDto;
import com.glennsyj.rivals.api.tft.model.match.TftMatchTrait;
import com.glennsyj.rivals.api.tft.model.match.TftMatchUnit;
import com.glennsyj.rivals.api.tft.model.match.TftMatchParticipantDto;
import com.glennsyj.rivals.api.tft.model.renew.TftRenewDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TftRenewController.class)
@Import(SecurityConfig.class)
@WithMockUser
class TftRenewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TftFacade tftFacade;

    @Test
    void TFT_데이터_갱신_성공() throws Exception {
        // given
        String gameName = "Hide";
        String tagLine = "KR1";
        
        TftStatusDto statusDto = new TftStatusDto(
            "RANKED_TFT",
            "DIAMOND",
            "I",
            75,
            10,
            20,
            true
        );

        TftRecentMatchDto matchDto = new TftRecentMatchDto(
            "1",
            "KR_1234567",
            1234567890L,
            35.5,
            8,
            1,
            "RANKED_TFT",
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList()
        );

        TftBadgeDto badgeDto = new TftBadgeDto(
            "FIRST_PLACE_MASTER",
            "PLACEMENT",
            1,
            5,
            true
        );

        TftRenewDto renewDto = new TftRenewDto(
            List.of(statusDto),
            List.of(matchDto),
            List.of(badgeDto),
            LocalDateTime.now()
        );

        when(tftFacade.renewAllTftData(gameName, tagLine))
                .thenReturn(renewDto);

        // when & then
        mockMvc.perform(get("/api/v1/tft/renew/{gameName}/{tagLine}", gameName, tagLine))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statuses[0].tier").value("DIAMOND"))
                .andExpect(jsonPath("$.statuses[0].rank").value("I"))
                .andExpect(jsonPath("$.statuses[0].leaguePoints").value(75))
                .andExpect(jsonPath("$.matches[0].placement").value(1))
                .andExpect(jsonPath("$.matches[0].level").value(8))
                .andExpect(jsonPath("$.badges[0].badgeType").value("FIRST_PLACE_MASTER"))
                .andExpect(jsonPath("$.badges[0].currentCount").value(1))
                .andExpect(jsonPath("$.badges[0].requiredCount").value(5))
                .andDo(print());
    }

    @Test
    void TFT_데이터_갱신_실패() throws Exception {
        // given
        String gameName = "NotExist";
        String tagLine = "KR1";

        when(tftFacade.renewAllTftData(gameName, tagLine))
                .thenThrow(new TftRenewException("Failed to renew TFT data", new RuntimeException()));

        // when & then
        mockMvc.perform(get("/api/v1/tft/renew/{gameName}/{tagLine}", gameName, tagLine))
                .andExpect(status().isInternalServerError())
                .andDo(print());
    }
} 