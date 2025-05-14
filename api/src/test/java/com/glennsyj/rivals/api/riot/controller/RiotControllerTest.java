package com.glennsyj.rivals.api.riot.controller;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.service.RiotAccountManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RiotController.class)
@WithMockUser
class RiotControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RiotAccountManager riotAccountManager;

    @Test
    void 계정_조회_성공() throws Exception {
        // given
        RiotAccount account = new RiotAccount("Hide", "KR1", "puuid123");
        when(riotAccountManager.findOrRegisterAccount("Hide", "KR1"))
                .thenReturn(account);

        // when & then
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/riot/accounts/{gameName}/{tagLine}", "Hide", "KR1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puuid").value("puuid123"))
                .andExpect(jsonPath("$.gameName").value("Hide"))
                .andExpect(jsonPath("$.tagLine").value("KR1"))
                .andDo(print());  // 응답 내용 출력
    }

    @Test
    void 계정_갱신_성공() throws Exception {
        // given
        RiotAccount account = new RiotAccount("Hide", "KR1", "puuid123");
        when(riotAccountManager.renewAccount("Hide", "KR1"))
                .thenReturn(account);

        // when & then
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/riot/accounts/renew/{gameName}/{tagLine}", "Hide", "KR1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puuid").value("puuid123"));
    }

    @Test
    void 존재하지_않는_계정_조회시_404() throws Exception {
        // given
        when(riotAccountManager.findOrRegisterAccount("NotExist", "KR1"))
                .thenThrow(new IllegalStateException("Account not found"));

        // when & then
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/riot/accounts/{gameName}/{tagLine}", "NotExist", "KR1"))
                .andExpect(status().isNotFound())
                .andDo(print());
    }
}