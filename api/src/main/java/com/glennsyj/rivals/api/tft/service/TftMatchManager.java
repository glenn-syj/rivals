package com.glennsyj.rivals.api.tft.service;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.tft.TftApiClient;
import com.glennsyj.rivals.api.tft.entity.entry.TftLeagueEntry;
import com.glennsyj.rivals.api.tft.entity.match.TftMatch;
import com.glennsyj.rivals.api.tft.model.match.TftMatchResponse;
import com.glennsyj.rivals.api.tft.repository.TftLeagueEntryRepository;
import com.glennsyj.rivals.api.tft.repository.TftMatchParticipantRepository;
import com.glennsyj.rivals.api.tft.repository.TftMatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class TftMatchManager {

    private final TftMatchParticipantRepository tftMatchParticipantRepository;
    private final TftLeagueEntryRepository tftLeagueEntryRepository;
    private final TftMatchRepository tftMatchRepository;
    private final TftApiClient tftApiClient;

    public TftMatchManager(TftMatchParticipantRepository tftMatchParticipantRepository,
                           TftLeagueEntryRepository tftLeagueEntryRepository,
                           TftMatchRepository tftMatchRepository,
                           TftApiClient tftApiClient) {
        this.tftMatchParticipantRepository = tftMatchParticipantRepository;
        this.tftLeagueEntryRepository = tftLeagueEntryRepository;
        this.tftMatchRepository = tftMatchRepository;
        this.tftApiClient = tftApiClient;
    }

    @Transactional
    public List<TftMatch> findOrCreateRecentTftMatches(Long accountId, String puuid) {

        /*
            TFT 매치 기록은 Entry 기록이 저장되어 있을 때에 종속 = TFT 플레이 기록이 있을 때만 확인하면 됨.
         */
        List<TftLeagueEntry> entries = tftLeagueEntryRepository.findLatestEntriesForEachQueueTypeByAccountId(accountId);

        // case 1: 사용자 entries가 null인 경우: Riot API 호출 필요 없음
        if (entries.isEmpty()) {
            return Collections.emptyList();
        }

        // case 2: 사용자 entries가 하나라도 있고 매치 기록도 있음: Riot API 호출 필요 없음
        List<TftMatch> existingMatches = tftMatchRepository.findTop20ByParticipantsPuuidOrderByGameCreationDesc(puuid);

        if (!existingMatches.isEmpty()) {
            return existingMatches;
        }

        // case 3: 사용자 entries가 하나라도 있는데 DB에 TFT Match가 없는 경우
        List<String> matchIds = tftApiClient.getMatchIdsFromPuuid(puuid);
        List<TftMatch> matches = matchIds.stream()
                .map(matchId -> {
                    TftMatchResponse response = tftApiClient.getMatchResponseFromMatchId(matchId);
                    return TftMatch.from(response);
                })
                .toList();

        // entries가 하나라도 있음 = TFT 전적 있음 != 매치 기록이 비어있음
        if (matches.isEmpty()) {
            throw new IllegalStateException("계정 갱신을 먼저 진행해주세요.");
        }

        return tftMatchRepository.saveAll(matches);
    }

    @Transactional
    public List<TftMatch> renewRecentTftMatches(String puuid) {

        List<String> recentMatchIds = tftApiClient.getMatchIdsFromPuuid(puuid);
        
        List<String> existingMatchIds = tftMatchRepository
                .findByMatchIdIn(recentMatchIds)
                .stream()
                .map(TftMatch::getMatchId)
                .toList();
        
        List<String> newMatchIds = recentMatchIds.stream()
                .filter(matchId -> !existingMatchIds.contains(matchId))
                .toList();
                
        List<TftMatch> newMatches = newMatchIds.stream()
                .map(matchId -> {
                    TftMatchResponse response = tftApiClient.getMatchResponseFromMatchId(matchId);
                    return TftMatch.from(response);
                })
                .toList();
                
        if (!newMatches.isEmpty()) {
            tftMatchRepository.saveAll(newMatches);
        }
        
        return tftMatchRepository.findTop20ByParticipantsPuuidOrderByGameCreationDesc(puuid);
    }

}
