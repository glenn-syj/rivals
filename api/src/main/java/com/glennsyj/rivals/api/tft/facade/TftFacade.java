package com.glennsyj.rivals.api.tft.facade;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.service.RiotAccountManager;
import com.glennsyj.rivals.api.tft.entity.entry.TftLeagueEntry;
import com.glennsyj.rivals.api.tft.entity.match.TftMatch;
import com.glennsyj.rivals.api.tft.facade.exception.TftRenewException;
import com.glennsyj.rivals.api.tft.model.badge.TftBadgeDto;
import com.glennsyj.rivals.api.tft.model.entry.TftStatusDto;
import com.glennsyj.rivals.api.tft.model.match.TftRecentMatchDto;
import com.glennsyj.rivals.api.tft.model.renew.TftRenewDto;
import com.glennsyj.rivals.api.tft.service.TftBadgeService;
import com.glennsyj.rivals.api.tft.service.TftLeagueEntryManager;
import com.glennsyj.rivals.api.tft.service.TftMatchManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class TftFacade {
    private final TftLeagueEntryManager tftLeagueEntryManager;
    private final TftMatchManager tftMatchManager;
    private final TftBadgeService tftBadgeService;
    private final RiotAccountManager riotAccountManager;
    private final Logger log = LoggerFactory.getLogger(TftFacade.class);

    public TftFacade(
            TftLeagueEntryManager tftLeagueEntryManager,
            TftMatchManager tftMatchManager,
            TftBadgeService tftBadgeService,
            RiotAccountManager riotAccountManager
    ) {
        this.tftLeagueEntryManager = tftLeagueEntryManager;
        this.tftMatchManager = tftMatchManager;
        this.tftBadgeService = tftBadgeService;
        this.riotAccountManager = riotAccountManager;
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
            log.error(errorMessage, e);
            throw new TftRenewException(errorMessage, e);
        }
    }
} 