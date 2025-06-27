package com.glennsyj.rivals.api.tft.service;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.tft.TftApiClient;
import com.glennsyj.rivals.api.tft.entity.entry.TftLeagueEntry;
import com.glennsyj.rivals.api.tft.entity.match.TftMatch;
import com.glennsyj.rivals.api.tft.model.match.MatchSyncResult;
import com.glennsyj.rivals.api.tft.model.match.TftMatchResponse;
import com.glennsyj.rivals.api.tft.repository.TftLeagueEntryRepository;
import com.glennsyj.rivals.api.tft.repository.TftMatchParticipantRepository;
import com.glennsyj.rivals.api.tft.repository.TftMatchRepository;
import com.glennsyj.rivals.api.riot.repository.RiotAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TftMatchManager {

    private static final int RECENT_MATCHES_LIMIT = 20;

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
    public MatchSyncResult findOrCreateRecentTftMatches(Long accountId, String puuid) {
        // 1. 기존 매치 조회
        List<TftMatch> existingMatches = tftMatchRepository.findTop20ByParticipantsPuuidOrderByGameCreationDesc(puuid);
        Set<String> existingMatchIds = existingMatches.stream()
                .map(TftMatch::getMatchId)
                .collect(Collectors.toSet());

        // 2. Riot API에서 최근 매치 ID 조회
        List<String> recentMatchIds = tftApiClient.getMatchIdsFromPuuid(puuid);
        if (recentMatchIds.size() > RECENT_MATCHES_LIMIT) {
            recentMatchIds = recentMatchIds.subList(0, RECENT_MATCHES_LIMIT);
        }
        
        // 3. 새로운 매치 ID 필터링
        List<String> newMatchIds = recentMatchIds.stream()
                .filter(id -> !existingMatchIds.contains(id))
                .toList();

        // 4. 새로운 매치 상세 정보 조회 및 저장
        List<TftMatch> newMatches = newMatchIds.isEmpty() ? List.of() :
            Flux.fromIterable(newMatchIds)
                .delayElements(Duration.ofMillis(51))
                .flatMap(tftApiClient::getMatchResponseFromMatchIdMono)
                .map(TftMatch::from)
                .collectList()
                .block();

        if (!newMatches.isEmpty()) {
            tftMatchRepository.saveAll(newMatches);
        }

        // 5. 결과 반환 (새로운 매치가 있다면 다시 최신 20개 조회)
        List<TftMatch> finalMatches = !newMatches.isEmpty() 
            ? tftMatchRepository.findTop20ByParticipantsPuuidOrderByGameCreationDesc(puuid)
            : existingMatches;

        return MatchSyncResult.of(
            finalMatches,
            newMatches,
            LocalDateTime.now()
        );
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
        List<TftMatch> newMatches = newMatchIds.isEmpty() ? List.of() :
            Flux.fromIterable(newMatchIds)
                .delayElements(Duration.ofMillis(51))
                .flatMap(tftApiClient::getMatchResponseFromMatchIdMono)
                .map(TftMatch::from)
                .collectList()
                .block();
                
        if (!newMatches.isEmpty()) {
            tftMatchRepository.saveAll(newMatches);
        }
        
        // 배치 사이즈로 나누어 처리
        return tftMatchRepository.findTop20ByParticipantsPuuidOrderByGameCreationDesc(puuid)
            .stream()
            .map(match -> {
                // 각 매치에 대해 필요한 시점에 participants 초기화
                Hibernate.initialize(match.getParticipants());
                return match;
            })
            .toList();
    }
}
