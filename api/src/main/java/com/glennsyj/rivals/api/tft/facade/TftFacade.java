package com.glennsyj.rivals.api.tft.facade;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.service.RiotAccountManager;
import com.glennsyj.rivals.api.tft.entity.entry.TftLeagueEntry;
import com.glennsyj.rivals.api.tft.entity.match.TftMatch;
import com.glennsyj.rivals.api.tft.facade.exception.TftRenewException;
import com.glennsyj.rivals.api.tft.model.badge.TftBadgeDto;
import com.glennsyj.rivals.api.tft.model.entry.TftStatusDto;
import com.glennsyj.rivals.api.tft.model.match.MatchSyncResult;
import com.glennsyj.rivals.api.tft.model.match.TftRecentMatchDto;
import com.glennsyj.rivals.api.tft.model.renew.TftRenewDto;
import com.glennsyj.rivals.api.tft.service.TftBadgeService;
import com.glennsyj.rivals.api.tft.service.TftLeagueEntryManager;
import com.glennsyj.rivals.api.tft.service.TftMatchManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class TftFacade {
    private static final Logger logger = LoggerFactory.getLogger(TftFacade.class);
    
    private final TftLeagueEntryManager tftLeagueEntryManager;
    private final TftMatchManager tftMatchManager;
    private final TftBadgeService tftBadgeService;
    private final RiotAccountManager riotAccountManager;
    private final ApplicationEventPublisher eventPublisher;

    public TftFacade(
            TftLeagueEntryManager tftLeagueEntryManager,
            TftMatchManager tftMatchManager,
            TftBadgeService tftBadgeService,
            RiotAccountManager riotAccountManager,
            ApplicationEventPublisher eventPublisher
    ) {
        this.tftLeagueEntryManager = tftLeagueEntryManager;
        this.tftMatchManager = tftMatchManager;
        this.tftBadgeService = tftBadgeService;
        this.riotAccountManager = riotAccountManager;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TftRenewDto renewAllTftData(String gameName, String tagLine) {
        try {
            RiotAccount account = riotAccountManager.findOrRegisterAccount(gameName, tagLine);

            // 모든 데이터를 병렬로 갱신
            CompletableFuture<List<TftLeagueEntry>> leagueEntriesFuture = CompletableFuture
                    .supplyAsync(() -> tftLeagueEntryManager.renewEntry(account.getId(), account.getPuuid()));

            CompletableFuture<List<TftMatch>> matchesFuture = CompletableFuture
                    .supplyAsync(() -> tftMatchManager.renewRecentTftMatches(account.getPuuid()));

            CompletableFuture<List<TftBadgeDto>> badgesFuture = matchesFuture.thenApply(
                    matches -> tftBadgeService.renewAccountBadges(account));

            account.renewUpdatedAt();

            // 모든 비동기 작업 완료 대기
            CompletableFuture.allOf(
                    leagueEntriesFuture,
                    matchesFuture,
                    badgesFuture
            ).join();

            // 응답 데이터 구성
            return new TftRenewDto(
                    leagueEntriesFuture.join().stream().map(TftStatusDto::from).toList(),
                    matchesFuture.join().stream().map(
                            match -> TftRecentMatchDto.from(account.getPuuid(), match)).toList(),
                    badgesFuture.join(),
                    account.getUpdatedAt());

        } catch (Exception e) {
            String errorMessage = String.format("Failed to renew TFT data for %s#%s", gameName, tagLine);
            logger.error(errorMessage, e);
            throw new TftRenewException(errorMessage, e);
        }
    }

    /**
     * 매치 데이터를 조회하고, 필요한 경우 업적/뱃지를 동기적으로 처리합니다.
     */
    @Transactional
    public List<TftRecentMatchDto> findAndProcessMatches(String gameName, String tagLine) {
        RiotAccount account = riotAccountManager.findOrRegisterAccount(gameName, tagLine);
        
        List<TftMatch> matches;
        // updatedAt이 null인 경우에만 매치 데이터 조회 및 처리 진행
        if (account.getUpdatedAt() == null) {
            // 1. 매치 데이터 조회 및 상태 확인
            MatchSyncResult syncResult = tftMatchManager.findOrCreateRecentTftMatches(
                account.getId(), 
                account.getPuuid()
            );

            // 2. 새로운 매치가 있는 경우 동기적으로 배지 처리
            if (syncResult.hasNewMatches()) {
                for (TftMatch match : syncResult.newMatches()) {
                    tftBadgeService.processMatchAchievements(match);
                }
                tftBadgeService.renewAccountBadges(account);
            }

            matches = syncResult.allMatches();
        } else {
            // updatedAt이 null이 아닌 경우 DB에서 최근 20개 매치 조회
            matches = tftMatchManager.findOrCreateRecentTftMatches(account.getId(), account.getPuuid()).allMatches();
        }
        
        return matches.stream()
            .map(match -> TftRecentMatchDto.from(account.getPuuid(), match))
            .toList();
    }
} 