package com.glennsyj.rivals.api.tft.service;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.repository.RiotAccountRepository;
import com.glennsyj.rivals.api.tft.TftApiClient;
import com.glennsyj.rivals.api.tft.entity.TftLeagueEntry;
import com.glennsyj.rivals.api.tft.model.entry.TftLeagueEntryResponse;
import com.glennsyj.rivals.api.tft.repository.TftLeagueEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TftLeagueEntryManager {

    private final TftLeagueEntryRepository tftLeagueEntryRepository;
    private final RiotAccountRepository riotAccountRepository;
    private final TftApiClient tftApiClient;

    public TftLeagueEntryManager(
            TftLeagueEntryRepository tftLeagueEntryRepository,
            RiotAccountRepository riotAccountRepository,
            TftApiClient tftApiClient) {
        this.tftLeagueEntryRepository = tftLeagueEntryRepository;
        this.riotAccountRepository = riotAccountRepository;
        this.tftApiClient = tftApiClient;
    }

    /**
     * 처음 검색해서 Entry를 생성하거나 이미 DB에 존재하는 Entry를 검색하는 경우에 이용
     *
     *
     * @param accountId
     * @return
     *
     * @deprecated QueueType 구분 없이 임의의 최근 엔트리 1개만 반환하므로, 명확한 비즈니스 처리가 어려움.
     * findOrCreateLeagueEntries(accountId) 사용을 권장합니다
     */
    @Deprecated
    @Transactional
    public TftLeagueEntry findOrCreateEntry(Long accountId) {
        return tftLeagueEntryRepository.findFirstByAccount_IdOrderByUpdatedAtDesc(accountId)
            .orElseGet(() -> {
                RiotAccount account = riotAccountRepository.findById(accountId)
                    .orElseThrow(() -> new IllegalStateException("계정을 찾을 수 없습니다: " + accountId));

                List<TftLeagueEntryResponse> responses = tftApiClient.getLeagueEntries(account.getPuuid());

                if (responses.isEmpty()) {
                    throw new IllegalStateException("이번 시즌 TFT 랭크 기록이 존재하지 않습니다");
                }

                TftLeagueEntryResponse response = responses.get(0);
                TftLeagueEntry newEntry = new TftLeagueEntry(account, response);
                return tftLeagueEntryRepository.save(newEntry);
            });
    }

    /**
     * 처음 검색해서 QueueType 별 Entry 다수를 생성하거나 이미 DB에 존재하는 Entry 다수를 검색하는 경우에 이용
     *
     * @param accountId
     * @return
     *
     */
    @Transactional
    public List<TftLeagueEntry> findOrCreateLeagueEntries(Long accountId) {
        List<TftLeagueEntry> entries = tftLeagueEntryRepository.findLatestEntriesForEachQueueTypeByAccountId(accountId);

        if (!entries.isEmpty()) {
            return entries;
        }

        RiotAccount account = riotAccountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalStateException("계정을 찾을 수 없습니다: " + accountId));

        List<TftLeagueEntryResponse> responses = tftApiClient.getLeagueEntries(account.getPuuid());

        // TODO: responses 가 비어있는 경우에도 처리 필요
//        if (responses.isEmpty()) {
//            throw new IllegalStateException("이번 시즌 TFT 랭크 기록이 존재하지 않습니다");
//        }

        List<TftLeagueEntry> newEntries = new ArrayList<>(responses.size());

        if (responses.isEmpty()) {
            return newEntries;
        }


        for (TftLeagueEntryResponse response : responses) {
            newEntries.add(new TftLeagueEntry(account, response));
        }

        return tftLeagueEntryRepository.saveAll(newEntries);
    }

    /**
     * 존재하는 계정 갱신 시 Entry 정보 갱신
     *
     * @param puuid
     * @return
     */
    @Transactional
    public TftLeagueEntry renewEntry(String puuid) {

        List<TftLeagueEntryResponse> responses = tftApiClient.getLeagueEntries(puuid);
        if (responses.isEmpty()) {
            throw new IllegalStateException("이번 시즌 TFT 랭크 기록이 존재하지 않습니다");
        }

        TftLeagueEntryResponse response = responses.get(0);

        return tftLeagueEntryRepository.findByPuuid(puuid)
            .map(entry -> {
                entry.updateFrom(response);
                return entry;
            })
            .orElseGet(() -> {
                RiotAccount account = riotAccountRepository.findByPuuid(puuid)
                    .orElseThrow(() -> new IllegalStateException("계정을 찾을 수 없습니다: " + puuid));
                TftLeagueEntry newEntry = new TftLeagueEntry(account, response);
                return tftLeagueEntryRepository.save(newEntry);
            });
    }
}
