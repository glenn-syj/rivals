package com.glennsyj.rivals.api.tft.service;

import com.glennsyj.rivals.api.config.EntityTestUtil;
import com.glennsyj.rivals.api.tft.TftApiClient;
import com.glennsyj.rivals.api.tft.entity.match.TftMatch;
import com.glennsyj.rivals.api.tft.model.match.MatchSyncResult;
import com.glennsyj.rivals.api.tft.model.match.TftMatchInfo;
import com.glennsyj.rivals.api.tft.model.match.TftMatchMetadata;
import com.glennsyj.rivals.api.tft.model.match.TftMatchParticipant;
import com.glennsyj.rivals.api.tft.model.match.TftMatchResponse;
import com.glennsyj.rivals.api.tft.repository.TftMatchParticipantRepository;
import com.glennsyj.rivals.api.tft.repository.TftMatchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TftMatchManagerTest {

    @Mock
    private TftMatchParticipantRepository tftMatchParticipantRepository;

    @Mock
    private TftMatchRepository tftMatchRepository;

    @Mock
    private TftApiClient tftApiClient;

    @InjectMocks
    private TftMatchManager tftMatchManager;

    @Test
    @DisplayName("매치 조회 시 기존 매치가 없고 새로운 매치도 없는 경우")
    void whenNoMatches_thenReturnEmptyResult() {
        // given
        String puuid = "test-puuid";
        given(tftMatchRepository.findTop20ByParticipantsPuuidOrderByGameCreationDesc(puuid))
                .willReturn(Collections.emptyList());
        given(tftApiClient.getMatchIdsFromPuuid(puuid))
                .willReturn(Collections.emptyList());

        // when
        MatchSyncResult result = tftMatchManager.findOrCreateRecentTftMatches(1L, puuid);

        // then
        assertThat(result.allMatches()).isEmpty();
        assertThat(result.newMatches()).isEmpty();
        assertThat(result.hasNewMatches()).isFalse();
        verify(tftApiClient).getMatchIdsFromPuuid(puuid);
        verify(tftMatchRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("매치 조회 시 기존 매치만 있는 경우")
    void whenOnlyExistingMatches_thenReturnExistingMatches() {
        // given
        String puuid = "test-puuid";
        List<TftMatch> existingMatches = List.of(createTftMatch("match-1"));
        
        given(tftMatchRepository.findTop20ByParticipantsPuuidOrderByGameCreationDesc(puuid))
                .willReturn(existingMatches);
        given(tftApiClient.getMatchIdsFromPuuid(puuid))
                .willReturn(List.of("match-1"));

        // when
        MatchSyncResult result = tftMatchManager.findOrCreateRecentTftMatches(1L, puuid);

        // then
        assertThat(result.allMatches()).isEqualTo(existingMatches);
        assertThat(result.newMatches()).isEmpty();
        assertThat(result.hasNewMatches()).isFalse();
        verify(tftMatchRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("매치 조회 시 새로운 매치가 있는 경우")
    void whenNewMatchesExist_thenSaveAndReturnAllMatches() {
        // given
        String puuid = "test-puuid";
        List<String> matchIds = List.of("match-1", "match-2");
        TftMatchResponse mockResponse = createMockMatchResponse();
        List<TftMatch> newMatches = List.of(TftMatch.from(mockResponse));
        List<TftMatch> allMatches = new ArrayList<>(newMatches);

        given(tftMatchRepository.findTop20ByParticipantsPuuidOrderByGameCreationDesc(puuid))
                .willReturn(allMatches);
        given(tftApiClient.getMatchIdsFromPuuid(puuid))
                .willReturn(matchIds);
        given(tftApiClient.getMatchResponseFromMatchId(any()))
                .willReturn(mockResponse);
        given(tftMatchRepository.saveAll(any()))
                .willReturn(newMatches);

        // when
        MatchSyncResult result = tftMatchManager.findOrCreateRecentTftMatches(1L, puuid);

        // then
        assertThat(result.allMatches()).isEqualTo(allMatches);
        assertThat(result.newMatches()).hasSize(2);
        assertThat(result.hasNewMatches()).isTrue();
        verify(tftMatchRepository).saveAll(any());
    }

    @Test
    @DisplayName("매치 갱신 시 새로운 매치가 있는 경우")
    void whenRenewingWithNewMatches_thenSaveAndReturnMatches() {
        // given
        String puuid = "test-puuid";
        List<String> matchIds = List.of("match-1", "match-2", "match-3");
        List<TftMatch> existingMatches = List.of(createTftMatch("match-1"));
        TftMatchResponse mockResponse = createMockMatchResponse();
        List<TftMatch> allMatches = List.of(
            createTftMatch("match-1"),
            createTftMatch("match-2"),
            createTftMatch("match-3")
        );

        given(tftApiClient.getMatchIdsFromPuuid(puuid))
                .willReturn(matchIds);
        given(tftMatchRepository.findByMatchIdIn(matchIds))
                .willReturn(existingMatches);
        given(tftApiClient.getMatchResponseFromMatchId(any()))
                .willReturn(mockResponse);
        given(tftMatchRepository.findTop20ByParticipantsPuuidOrderByGameCreationDesc(puuid))
                .willReturn(allMatches);

        // when
        List<TftMatch> result = tftMatchManager.renewRecentTftMatches(puuid);

        // then
        assertThat(result).hasSize(3);
        verify(tftMatchRepository).saveAll(any());
        verify(tftApiClient, times(2)).getMatchResponseFromMatchId(any());
    }

    private TftMatchResponse createMockMatchResponse() {
        return new TftMatchResponse(
            createMockMatchMetadata(),
            createMockMatchInfo()
        );
    }

    private TftMatchMetadata createMockMatchMetadata() {
        return new TftMatchMetadata(
            "match-1",
            "version-1",
            List.of("test-puuid")
        );
    }

    private TftMatchInfo createMockMatchInfo() {
        return new TftMatchInfo(
            "result",           // endOfGameResult
            1000L,              // gameCreation
            2000L,              // gameId
            "variation",        // game_variation
            3000L,              // game_datetime
            10.0,               // game_length
            "game-version",     // game_version
            1,                  // mapId
            createMockParticipants(),  // participants
            1,                  // queueId
            1,                  // queue_id
            "type",             // tft_game_type
            "core",             // tft_set_core_name
            1                   // tft_set_number
        );
    }

    private List<TftMatchParticipant> createMockParticipants() {
        return List.of(new TftMatchParticipant(
            null,               // companion
            100,               // gold_left
            8,                 // last_round
            8,                 // level
            Map.of("PlayerScore2", 1000),  // missions
            4,                 // placement
            3,                 // players_eliminated
            "test-puuid",      // puuid
            "TestPlayer",      // riotIdGameName
            "TEST",            // riotIdTagline
            20.0,              // time_eliminated
            1000,              // total_damage_to_players
            Collections.emptyList(),  // traits
            Collections.emptyList(),   // units
            false             // win
        ));
    }

    private TftMatch createTftMatch(String matchId) {
        TftMatchResponse response = createMockMatchResponse();
        TftMatch match = TftMatch.from(response);
        EntityTestUtil.setFieldWithValue(match, "matchId", matchId);
        return match;
    }
}
