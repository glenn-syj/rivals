package com.glennsyj.rivals.api.tft.service;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.tft.TftApiClient;
import com.glennsyj.rivals.api.tft.entity.entry.TftLeagueEntry;
import com.glennsyj.rivals.api.tft.entity.match.TftMatch;
import com.glennsyj.rivals.api.tft.model.match.TftMatchResponse;
import com.glennsyj.rivals.api.tft.repository.TftLeagueEntryRepository;
import com.glennsyj.rivals.api.tft.repository.TftMatchParticipantRepository;
import com.glennsyj.rivals.api.tft.repository.TftMatchRepository;
import com.glennsyj.rivals.api.riot.repository.RiotAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class TftMatchManager {

    private final TftMatchParticipantRepository tftMatchParticipantRepository;
    private final TftLeagueEntryRepository tftLeagueEntryRepository;
    private final TftMatchRepository tftMatchRepository;
    private final RiotAccountRepository riotAccountRepository;
    private final TftApiClient tftApiClient;
    private final TftBadgeService tftBadgeService;

    public TftMatchManager(TftMatchParticipantRepository tftMatchParticipantRepository,
                           TftLeagueEntryRepository tftLeagueEntryRepository,
                           TftMatchRepository tftMatchRepository,
                           RiotAccountRepository riotAccountRepository,
                           TftApiClient tftApiClient,
                           TftBadgeService tftBadgeService) {
        this.tftMatchParticipantRepository = tftMatchParticipantRepository;
        this.tftLeagueEntryRepository = tftLeagueEntryRepository;
        this.tftMatchRepository = tftMatchRepository;
        this.riotAccountRepository = riotAccountRepository;
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

        // RiotAccount를 가져옴 (accountId가 파라미터로 왔다는 것은 이미 존재한다는 의미)
        RiotAccount riotAccount = riotAccountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalStateException("RiotAccount가 존재하지 않습니다."));

        // UpdatedAt이 null이면 아직 한 번도 갱신되지 않은 계정
        boolean needsInitialFetch = riotAccount.getUpdatedAt() == null;

        // 첫 갱신이 필요한 경우, Riot API에서 최신 매치들을 가져옴
        if (needsInitialFetch) {
            List<String> recentMatchIds = tftApiClient.getMatchIdsFromPuuid(puuid);
            if (recentMatchIds.isEmpty()) {
                throw new IllegalStateException("계정 갱신을 먼저 진행해주세요.");
            }

            // DB에 이미 존재하는 매치들을 찾음
            List<TftMatch> existingMatches = tftMatchRepository.findByMatchIdIn(recentMatchIds);
            
            // 새로 가져와야 할 매치 ID들을 필터링
            List<String> newMatchIds = recentMatchIds.stream()
                    .filter(matchId -> existingMatches.stream()
                            .noneMatch(match -> match.getMatchId().equals(matchId)))
                    .toList();

            // 새로운 매치들을 API에서 가져와서 저장
            if (!newMatchIds.isEmpty()) {
                List<TftMatch> newMatches = newMatchIds.stream()
                        .map(matchId -> {
                            TftMatchResponse response = tftApiClient.getMatchResponseFromMatchId(matchId);
                            return TftMatch.from(response);
                        })
                        .toList();
                
                List<TftMatch> savedNewMatches = tftMatchRepository.saveAll(newMatches);
                
                // 새로운 매치에 대해 뱃지 처리
                for (TftMatch match : savedNewMatches) {
                    tftBadgeService.processMatchAchievements(match);
                }
                
                // 기존 매치와 새 매치를 합침
                existingMatches.addAll(savedNewMatches);
            }

            // 최신 순으로 정렬하여 반환
            return existingMatches.stream()
                    .sorted((m1, m2) -> Long.compare(m2.getGameCreation(), m1.getGameCreation()))
                    .limit(20)
                    .toList();
        }

        // 이미 한 번이라도 검색이 진행된 계정이면 DB에 있는 매치만 반환
        return tftMatchRepository.findTop20ByParticipantsPuuidOrderByGameCreationDesc(puuid);
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
