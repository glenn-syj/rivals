package com.glennsyj.rivals.api.tft.service;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.repository.RiotAccountRepository;
import com.glennsyj.rivals.api.tft.TftApiClient;
import com.glennsyj.rivals.api.tft.entity.TftLeagueEntry;
import com.glennsyj.rivals.api.tft.model.TftLeagueEntryResponse;
import com.glennsyj.rivals.api.tft.repository.TftLeagueEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
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
     * @param accountId
     * @return
     */
    @Transactional
    public TftLeagueEntry findOrCreateEntry(Long accountId) {
        return tftLeagueEntryRepository.findByAccount_Id(accountId)
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
