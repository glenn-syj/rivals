package com.glennsyj.rivals.api.tft.controller;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.service.RiotAccountManager;
import com.glennsyj.rivals.api.tft.entity.achievement.TftBadgeProgress;
import com.glennsyj.rivals.api.tft.model.badge.TftBadgeDto;
import com.glennsyj.rivals.api.tft.service.TftBadgeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tft/badges")
public class TftBadgeController {

    private final TftBadgeService tftBadgeService;
    private final RiotAccountManager riotAccountManager;

    public TftBadgeController(TftBadgeService tftBadgeService, RiotAccountManager riotAccountManager) {
        this.tftBadgeService = tftBadgeService;
        this.riotAccountManager = riotAccountManager;
    }

    @GetMapping("/{gameName}/{tagLine}")
    public ResponseEntity<List<TftBadgeDto>> findAllBadges(
            @PathVariable String gameName,
            @PathVariable String tagLine) {
        try {
            RiotAccount account = riotAccountManager.findOrRegisterAccount(gameName, tagLine);
            List<TftBadgeDto> badges = tftBadgeService.findAllBadges(account);
            return ResponseEntity.ok(badges);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{gameName}/{tagLine}/{badgeType}")
    public ResponseEntity<TftBadgeDto> findBadge(
            @PathVariable String gameName,
            @PathVariable String tagLine,
            @PathVariable String badgeType) {
        try {
            RiotAccount account = riotAccountManager.findOrRegisterAccount(gameName, tagLine);
            return tftBadgeService.findBadge(account, TftBadgeProgress.BadgeType.valueOf(badgeType))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 