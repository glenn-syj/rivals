package com.glennsyj.rivals.api.tft.service;

import com.glennsyj.rivals.api.config.EntityTestUtil;
import com.glennsyj.rivals.api.tft.TftApiClient;
import com.glennsyj.rivals.api.tft.entity.entry.TftLeagueEntry;
import com.glennsyj.rivals.api.tft.entity.match.TftMatch;
import com.glennsyj.rivals.api.tft.model.match.TftMatchInfo;
import com.glennsyj.rivals.api.tft.model.match.TftMatchMetadata;
import com.glennsyj.rivals.api.tft.model.match.TftMatchParticipant;
import com.glennsyj.rivals.api.tft.model.match.TftMatchResponse;
import com.glennsyj.rivals.api.tft.repository.TftLeagueEntryRepository;
import com.glennsyj.rivals.api.tft.repository.TftMatchParticipantRepository;
import com.glennsyj.rivals.api.tft.repository.TftMatchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TftMatchManagerTest {

    @Mock
    private TftMatchParticipantRepository tftMatchParticipantRepository;

    @Mock
    private TftLeagueEntryRepository tftLeagueEntryRepository;

    @Mock
    private TftMatchRepository tftMatchRepository;

    @Mock
    private TftApiClient tftApiClient;

    @InjectMocks
    private TftMatchManager tftMatchManager;

    @Test
    @DisplayName("TFT 엔트리가 없는 경우 빈 리스트를 반환한다")
    void whenNoEntries_thenReturnEmptyList() {
        // given
        Long accountId = 1L;
        String puuid = "test-puuid";
        given(tftLeagueEntryRepository.findLatestEntriesForEachQueueTypeByAccountId(accountId))
                .willReturn(Collections.emptyList());

        // when
        List<TftMatch> result = tftMatchManager.findOrCreateRecentTftMatches(accountId, puuid);

        // then
        assertThat(result).isEmpty();
        verify(tftApiClient, never()).getMatchIdsFromPuuid(any());
    }

    @Test
    @DisplayName("TFT 엔트리가 있고 매치 기록도 있는 경우 기존 매치를 반환한다")
    void whenHasEntriesAndMatches_thenReturnExistingMatches() {
        // given
        Long accountId = 1L;
        String puuid = "test-puuid";
        List<TftLeagueEntry> entries = List.of(mock(TftLeagueEntry.class));
        List<TftMatch> existingMatches = List.of(mock(TftMatch.class));

        given(tftLeagueEntryRepository.findLatestEntriesForEachQueueTypeByAccountId(accountId))
                .willReturn(entries);
        given(tftMatchRepository.findTop20ByParticipantsPuuidOrderByGameCreationDesc(puuid))
                .willReturn(existingMatches);

        // when
        List<TftMatch> result = tftMatchManager.findOrCreateRecentTftMatches(accountId, puuid);

        // then
        assertThat(result).isEqualTo(existingMatches);
        verify(tftApiClient, never()).getMatchIdsFromPuuid(any());
    }

    @Test
    @DisplayName("TFT 엔트리는 있지만 매치 기록이 없는 경우 API를 호출하여 매치를 생성한다")
    void whenHasEntriesButNoMatches_thenCreateNewMatches() {
        // given
        Long accountId = 1L;
        String puuid = "test-puuid";
        List<TftLeagueEntry> entries = List.of(mock(TftLeagueEntry.class));
        List<String> matchIds = List.of("match-1", "match-2");

        // Create mock response using helper method
        TftMatchResponse mockResponse = createMockMatchResponse();

        given(tftLeagueEntryRepository.findLatestEntriesForEachQueueTypeByAccountId(accountId))
                .willReturn(entries);
        given(tftMatchRepository.findTop20ByParticipantsPuuidOrderByGameCreationDesc(puuid))
                .willReturn(Collections.emptyList());
        given(tftApiClient.getMatchIdsFromPuuid(puuid)).willReturn(matchIds);
        given(tftApiClient.getMatchResponseFromMatchId(any())).willReturn(mockResponse);
        given(tftMatchRepository.saveAll(any())).willAnswer(invocation -> {
            List<TftMatch> matches = invocation.getArgument(0);
            return matches;
        });

        // when
        List<TftMatch> result = tftMatchManager.findOrCreateRecentTftMatches(accountId, puuid);

        // then
        assertThat(result).isNotEmpty();
        verify(tftApiClient).getMatchIdsFromPuuid(puuid);
        verify(tftMatchRepository).saveAll(any());
    }

    @Test
    @DisplayName("TFT 엔트리는 있지만 API에서 매치를 찾을 수 없는 경우 예외가 발생한다")
    void whenHasEntriesButNoMatchesFromApi_thenThrowException() {
        // given
        Long accountId = 1L;
        String puuid = "test-puuid";
        List<TftLeagueEntry> entries = List.of(mock(TftLeagueEntry.class));

        given(tftLeagueEntryRepository.findLatestEntriesForEachQueueTypeByAccountId(accountId))
                .willReturn(entries);
        given(tftMatchRepository.findTop20ByParticipantsPuuidOrderByGameCreationDesc(puuid))
                .willReturn(Collections.emptyList());
        given(tftApiClient.getMatchIdsFromPuuid(puuid)).willReturn(Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> tftMatchManager.findOrCreateRecentTftMatches(accountId, puuid))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("계정 갱신을 먼저 진행해주세요.");
    }

    @Test
    @DisplayName("갱신 시 새로운 매치가 있는 경우 새 매치를 저장하고 최근 20개 매치를 반환한다")
    void whenRenewingWithNewMatches_thenSaveAndReturnMatches() {
        // given
        String puuid = "test-puuid";
        List<String> recentMatchIds = List.of("match-1", "match-2", "match-3");
        List<TftMatch> existingMatches = List.of(
            createTftMatch("match-1"),
            createTftMatch("match-2")
        );
        TftMatchResponse mockResponse = createMockMatchResponse();

        given(tftApiClient.getMatchIdsFromPuuid(puuid)).willReturn(recentMatchIds);
        given(tftMatchRepository.findByMatchIdIn(recentMatchIds)).willReturn(existingMatches);
        given(tftApiClient.getMatchResponseFromMatchId("match-3")).willReturn(mockResponse);
        given(tftMatchRepository.findTop20ByParticipantsPuuidOrderByGameCreationDesc(puuid))
            .willReturn(List.of(createTftMatch("match-1"), createTftMatch("match-2"), createTftMatch("match-3")));

        // when
        List<TftMatch> result = tftMatchManager.renewRecentTftMatches(puuid);

        // then
        verify(tftMatchRepository).saveAll(any());
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("갱신 시 모든 매치가 이미 존재하는 경우 API를 호출하지 않고 기존 매치를 반환한다")
    void whenRenewingWithAllExistingMatches_thenReturnExistingMatches() {
        // given
        String puuid = "test-puuid";
        List<String> recentMatchIds = List.of("match-1", "match-2");
        List<TftMatch> existingMatches = List.of(
            createTftMatch("match-1"),
            createTftMatch("match-2")
        );

        given(tftApiClient.getMatchIdsFromPuuid(puuid)).willReturn(recentMatchIds);
        given(tftMatchRepository.findByMatchIdIn(recentMatchIds)).willReturn(existingMatches);
        given(tftMatchRepository.findTop20ByParticipantsPuuidOrderByGameCreationDesc(puuid))
            .willReturn(existingMatches);

        // when
        List<TftMatch> result = tftMatchManager.renewRecentTftMatches(puuid);

        // then
        verify(tftApiClient, never()).getMatchResponseFromMatchId(any());
        verify(tftMatchRepository, never()).saveAll(any());
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("갱신 시 API 호출에 실패하면 예외가 발생한다")
    void whenRenewingWithApiFailure_thenThrowException() {
        // given
        String puuid = "test-puuid";
        List<String> recentMatchIds = List.of("match-1", "match-2", "match-3");
        List<TftMatch> existingMatches = List.of(createTftMatch("match-1"));

        given(tftApiClient.getMatchIdsFromPuuid(puuid)).willReturn(recentMatchIds);
        given(tftMatchRepository.findByMatchIdIn(recentMatchIds)).willReturn(existingMatches);
        given(tftApiClient.getMatchResponseFromMatchId("match-2"))
            .willThrow(new RuntimeException("API 호출 실패"));

        // when & then
        assertThatThrownBy(() -> tftMatchManager.renewRecentTftMatches(puuid))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("API 호출 실패");
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
            List.of("test-puuid")  // participants puuid list
        );
    }

    private TftMatchInfo createMockMatchInfo() {
        return new TftMatchInfo(
                "result",           // endOfGameResult (첫 번째 파라미터)
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

    private List<com.glennsyj.rivals.api.tft.model.match.TftMatchParticipant> createMockParticipants() {
        // TftMatchInfo는 model.match.TftMatchParticipant를 사용해야 함
        return List.of(new com.glennsyj.rivals.api.tft.model.match.TftMatchParticipant(
                null,
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
