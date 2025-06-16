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

    private final TftBadgeService tftBadgeService;

    public TftMatchManager(TftMatchParticipantRepository tftMatchParticipantRepository,
                           TftLeagueEntryRepository tftLeagueEntryRepository,
                           TftMatchRepository tftMatchRepository,
                           TftApiClient tftApiClient,
                           TftBadgeService tftBadgeService) {
        this.tftMatchParticipantRepository = tftMatchParticipantRepository;
        this.tftLeagueEntryRepository = tftLeagueEntryRepository;
        this.tftMatchRepository = tftMatchRepository;
        this.tftApiClient = tftApiClient;
        this.tftBadgeService = tftBadgeService;
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

        List<TftMatch> result = tftMatchRepository.saveAll(matches);

        for (TftMatch match : result) {
            tftBadgeService.processMatchAchievements(match);
        }

        return result;
    }

    @Transactional
    public List<TftMatch> renewRecentTftMatches(String puuid) {
        
        // 최신 Match Id부터 받아옴
        List<String> recentMatchIds = tftApiClient.getMatchIdsFromPuuid(puuid);
        
        // 최신 Match Id 리스트에서 DB에 존재하는 TftMatch 엔티티를 제외하기 위한 리스트 생성
        List<String> existingMatchIds = tftMatchRepository
                .findByMatchIdIn(recentMatchIds)
                .stream()
                .map(TftMatch::getMatchId)
                .toList();
        
        // 최신 MatchId에서 이미 존재하는 MatchId는 제외
        List<String> newMatchIds = recentMatchIds.stream()
                .filter(matchId -> !existingMatchIds.contains(matchId))
                .toList();
                
        // 새로운 MatchId에 대해서 MatchResponse 받아옴
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
