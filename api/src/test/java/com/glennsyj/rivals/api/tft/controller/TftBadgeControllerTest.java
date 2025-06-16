package com.glennsyj.rivals.api.tft.controller;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.service.RiotAccountManager;
import com.glennsyj.rivals.api.tft.entity.achievement.TftBadgeProgress;
import com.glennsyj.rivals.api.tft.model.badge.TftBadgeDto;
import com.glennsyj.rivals.api.tft.service.TftBadgeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TftBadgeController.class)
@WithMockUser
class TftBadgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TftBadgeService tftBadgeService;

    @MockitoBean
    private RiotAccountManager riotAccountManager;

    @Test
    void findAllBadges_ShouldReturnBadges() throws Exception {
        // Given
        String gameName = "testUser";
        String tagLine = "KR1";
        RiotAccount account = new RiotAccount("puuid", gameName, tagLine);

        TftBadgeDto mvpBadge = new TftBadgeDto(
            "MVP",
            "FIRST_PLACE",
            6,
            6,
            true
        );

        when(riotAccountManager.findOrRegisterAccount(gameName, tagLine))
            .thenReturn(account);
        when(tftBadgeService.findAllBadges(account))
            .thenReturn(List.of(mvpBadge));

        // When & Then
        mockMvc.perform(get("/api/v1/tft/badges/{gameName}/{tagLine}", gameName, tagLine))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].badgeType").value("MVP"))
            .andExpect(jsonPath("$[0].achievementType").value("FIRST_PLACE"))
            .andExpect(jsonPath("$[0].currentCount").value(6))
            .andExpect(jsonPath("$[0].requiredCount").value(6))
            .andExpect(jsonPath("$[0].isActive").value(true));
    }

    @Test
    void findBadge_ShouldReturnSpecificBadge() throws Exception {
        // Given
        String gameName = "testUser";
        String tagLine = "KR1";
        String badgeType = "MVP";
        RiotAccount account = new RiotAccount("puuid", gameName, tagLine);

        TftBadgeDto mvpBadge = new TftBadgeDto(
            badgeType,
            "FIRST_PLACE",
            6,
            6,
            true
        );

        when(riotAccountManager.findOrRegisterAccount(gameName, tagLine))
            .thenReturn(account);
        when(tftBadgeService.findBadge(eq(account), any(TftBadgeProgress.BadgeType.class)))
            .thenReturn(Optional.of(mvpBadge));

        // When & Then
        mockMvc.perform(get("/api/v1/tft/badges/{gameName}/{tagLine}/{badgeType}", 
                gameName, tagLine, badgeType))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.badgeType").value("MVP"))
            .andExpect(jsonPath("$.achievementType").value("FIRST_PLACE"))
            .andExpect(jsonPath("$.currentCount").value(6))
            .andExpect(jsonPath("$.requiredCount").value(6))
            .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void findBadge_ShouldReturn404WhenBadgeNotFound() throws Exception {
        // Given
        String gameName = "testUser";
        String tagLine = "KR1";
        String badgeType = "MVP";
        RiotAccount account = new RiotAccount("puuid", gameName, tagLine);

        when(riotAccountManager.findOrRegisterAccount(gameName, tagLine))
            .thenReturn(account);
        when(tftBadgeService.findBadge(eq(account), any(TftBadgeProgress.BadgeType.class)))
            .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/tft/badges/{gameName}/{tagLine}/{badgeType}", 
                gameName, tagLine, badgeType))
            .andExpect(status().isNotFound());
    }

    @Test
    void findBadge_ShouldReturn400WhenInvalidBadgeType() throws Exception {
        // Given
        String gameName = "testUser";
        String tagLine = "KR1";
        String badgeType = "INVALID_BADGE_TYPE";

        when(riotAccountManager.findOrRegisterAccount(gameName, tagLine))
            .thenReturn(new RiotAccount("puuid", gameName, tagLine));

        // When & Then
        mockMvc.perform(get("/api/v1/tft/badges/{gameName}/{tagLine}/{badgeType}", 
                gameName, tagLine, badgeType))
            .andExpect(status().isBadRequest());
    }
} 