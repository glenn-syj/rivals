package com.glennsyj.rivals.api.tft.service;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.repository.RiotAccountRepository;
import com.glennsyj.rivals.api.tft.TftApiClient;
import com.glennsyj.rivals.api.tft.entity.TftLeagueEntry;
import com.glennsyj.rivals.api.tft.model.TftLeagueEntryResponse;
import com.glennsyj.rivals.api.tft.repository.TftLeagueEntryRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TftLeagueEntryManagerTest {
    
    @Mock
    private TftLeagueEntryRepository tftLeagueEntryRepository;
    
    @Mock
    private RiotAccountRepository riotAccountRepository;
    
    @Mock
    private TftApiClient tftApiClient;
    
    @InjectMocks
    private TftLeagueEntryManager tftLeagueEntryManager;

    @Test
    @Deprecated
    @Disabled("Deprecated 메서드 테스트 제외")
    @DisplayName("findOrCreateEntry: 존재하는 엔트리를 정상적으로 반환한다")
    void findOrCreateEntry_ShouldReturnExistingEntry() {
        // given
        Long accountId = 1L;
        TftLeagueEntry existingEntry = createMockEntry();
        when(tftLeagueEntryRepository.findByAccount_Id(accountId))
            .thenReturn(Optional.of(existingEntry));

        // when
        TftLeagueEntry result = tftLeagueEntryManager.findOrCreateEntry(accountId);

        // then
        assertThat(result).isEqualTo(existingEntry);
        verify(tftApiClient, never()).getLeagueEntries(anyString());
    }

    @Test
    @Deprecated
    @Disabled("Deprecated 메서드 테스트 제외")
    @DisplayName("findOrCreateEntry: 신규 엔트리를 생성하고 저장한다")
    void findOrCreateEntry_ShouldCreateAndSaveNewEntry() {
        // given
        RiotAccount account = new RiotAccount("test", "KR1", "test-puuid");
        Long accountId = 1L;
        TftLeagueEntryResponse response = createMockResponse();
        List<TftLeagueEntryResponse> responses = List.of(response);

        when(tftLeagueEntryRepository.findByAccount_Id(accountId))
            .thenReturn(Optional.empty());
        when(riotAccountRepository.findById(accountId))
            .thenReturn(Optional.of(account));
        when(tftApiClient.getLeagueEntries(account.getPuuid()))
            .thenReturn(responses);
        when(tftLeagueEntryRepository.save(any()))
            .thenReturn(new TftLeagueEntry(account, response));

        // when
        TftLeagueEntry result = tftLeagueEntryManager.findOrCreateEntry(accountId);

        // then
        assertThat(result.getAccount()).isEqualTo(account);
        verify(tftLeagueEntryRepository).save(any(TftLeagueEntry.class));
    }

    @Test
    @DisplayName("renewEntry: 기존 엔트리를 갱신한다")
    void renewEntry_ShouldUpdateExistingEntry() {
        // given
        String puuid = "test-puuid";
        TftLeagueEntry existingEntry = createMockEntry();
        TftLeagueEntryResponse response = createMockResponse();
        
        when(tftApiClient.getLeagueEntries(puuid))
            .thenReturn(List.of(response));
        when(tftLeagueEntryRepository.findByPuuid(puuid))
            .thenReturn(Optional.of(existingEntry));

        // when
        TftLeagueEntry result = tftLeagueEntryManager.renewEntry(puuid);

        // then
        assertThat(result).isEqualTo(existingEntry);
        verify(tftLeagueEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("findOrCreateLeagueEntries: 존재하는 큐타입별 엔트리들을 정상적으로 반환한다")
    void findOrCreateLeagueEntries_ShouldReturnExistingEntries() {
        // given
        Long accountId = 1L;
        TftLeagueEntry existingEntry1 = createMockEntry();
        TftLeagueEntry existingEntry2 = createMockEntryWithQueueType("RANKED_TFT_TURBO");
        List<TftLeagueEntry> existingEntries = List.of(existingEntry1, existingEntry2);

        when(tftLeagueEntryRepository.findLatestEntriesForEachQueueTypeByAccountId(accountId))
            .thenReturn(existingEntries);

        // when
        List<TftLeagueEntry> result = tftLeagueEntryManager.findOrCreateLeagueEntries(accountId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrderElementsOf(existingEntries);
        verify(tftApiClient, never()).getLeagueEntries(anyString());
    }

    @Test
    @DisplayName("findOrCreateLeagueEntries: 신규 큐타입별 엔트리들을 생성하고 저장한다")
    void findOrCreateLeagueEntries_ShouldCreateAndSaveNewEntries() {
        // given
        Long accountId = 1L;
        RiotAccount account = new RiotAccount("test", "KR1", "test-puuid");
        TftLeagueEntryResponse response1 = createMockResponse();
        TftLeagueEntryResponse response2 = createMockResponseWithQueueType("RANKED_TFT_TURBO");
        List<TftLeagueEntryResponse> responses = List.of(response1, response2);

        when(tftLeagueEntryRepository.findLatestEntriesForEachQueueTypeByAccountId(accountId))
            .thenReturn(List.of());
        when(riotAccountRepository.findById(accountId))
            .thenReturn(Optional.of(account));
        when(tftApiClient.getLeagueEntries(account.getPuuid()))
            .thenReturn(responses);
        when(tftLeagueEntryRepository.saveAll(any()))
            .thenReturn(List.of(
                new TftLeagueEntry(account, response1),
                new TftLeagueEntry(account, response2)
            ));

        // when
        List<TftLeagueEntry> result = tftLeagueEntryManager.findOrCreateLeagueEntries(accountId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(entry -> entry.getAccount().equals(account));
        verify(tftLeagueEntryRepository).saveAll(any());
    }

    private TftLeagueEntry createMockEntry() {
        RiotAccount account = new RiotAccount("test", "KR1", "test-puuid");
        return new TftLeagueEntry(account, createMockResponse());
    }

    private TftLeagueEntryResponse createMockResponse() {
        return new TftLeagueEntryResponse(
            "test-puuid",
            "test-league-id",
            "test-summoner-id",
            "RANKED_TFT",
            "DIAMOND",
            "I",
            100,
            10,
            5,
            false,
            false,
            false,
            false,
            null
        );
    }

    private TftLeagueEntry createMockEntryWithQueueType(String queueType) {
        RiotAccount account = new RiotAccount("test", "KR1", "test-puuid");
        return new TftLeagueEntry(account, createMockResponseWithQueueType(queueType));
    }

    private TftLeagueEntryResponse createMockResponseWithQueueType(String queueType) {
        return new TftLeagueEntryResponse(
            "test-puuid",
            "test-league-id",
            "test-summoner-id",
            queueType,
            "DIAMOND",
            "I",
            100,
            10,
            5,
            false,
            false,
            false,
            false,
            null
        );
    }
} 