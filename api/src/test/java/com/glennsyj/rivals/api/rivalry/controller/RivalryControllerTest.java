package com.glennsyj.rivals.api.rivalry.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glennsyj.rivals.api.rivalry.entity.RivalSide;
import com.glennsyj.rivals.api.rivalry.model.RivalryCreationDto;
import com.glennsyj.rivals.api.rivalry.model.RivalryDetailDto;
import com.glennsyj.rivals.api.rivalry.model.RivalryParticipantDto;
import com.glennsyj.rivals.api.rivalry.service.RivalryService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RivalryController.class)
@WithMockUser
class RivalryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RivalryService rivalryService;

    @Test
    @DisplayName("라이벌리 생성 요청이 성공하면 201 Created를 반환한다")
    void createRivalry_Success() throws Exception {
        // given
        List<RivalryParticipantDto> participants = List.of(
                new RivalryParticipantDto(1L, RivalSide.LEFT),
                new RivalryParticipantDto(2L, RivalSide.RIGHT)
        );
        RivalryCreationDto creationDto = new RivalryCreationDto(participants);
        Long rivalryId = 1L;

        String content = new ObjectMapper().writeValueAsString(creationDto);
        System.out.println(content);

        when(rivalryService.createRivalryFrom(any(RivalryCreationDto.class)))
                .thenReturn(rivalryId);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/rivalries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/rivalries/" + rivalryId))
                .andExpect(jsonPath("$.rivalryId").value(rivalryId))
                .andDo(print());
    }

    @Test
    @DisplayName("잘못된 형식의 라이벌리 생성 요청이 오면 400 Bad Request를 반환한다")
    void createRivalry_BadRequest() throws Exception {
        // given
        String invalidContent = "{\"participants\":[]}"; // 빈 participants 리스트를 포함한 JSON

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/rivalries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidContent)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("존재하는 라이벌리 ID로 조회하면 200 OK와 상세 정보를 반환한다")
    void getRivalryById_Success() throws Exception {
        // given
        Long rivalryId = 1L;
        RivalryDetailDto detailDto = new RivalryDetailDto(
                rivalryId,
                List.of(),
                List.of(),
                LocalDateTime.now()
        );

        when(rivalryService.findRivalryFrom(rivalryId)).thenReturn(detailDto);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/rivalries/" + rivalryId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rivalryId").value(rivalryId))
                .andExpect(jsonPath("$.leftStats").isArray())
                .andExpect(jsonPath("$.rightStats").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("존재하지 않는 라이벌리 ID로 조회하면 404 Not Found를 반환한다")
    void getRivalryById_NotFound() throws Exception {
        // given
        Long nonExistentId = 999L;
        when(rivalryService.findRivalryFrom(nonExistentId))
                .thenThrow(new EntityNotFoundException());

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/rivalries/" + nonExistentId)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andDo(print());
    }
}
