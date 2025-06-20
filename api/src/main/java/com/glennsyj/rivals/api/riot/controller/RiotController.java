package com.glennsyj.rivals.api.riot.controller;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.model.RiotAccountDto;
import com.glennsyj.rivals.api.riot.model.RiotAccountResponse;
import com.glennsyj.rivals.api.riot.service.RiotAccountManager;
import com.glennsyj.rivals.api.tft.service.TftLeagueEntryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/riot")
public class RiotController {

    private final RiotAccountManager riotAccountManager;

    private final Logger log = LoggerFactory.getLogger(RiotController.class);

    public RiotController(RiotAccountManager riotAccountManager) {
        this.riotAccountManager = riotAccountManager;
    }

    @GetMapping("/accounts/{gameName}/{tagLine}")
    public ResponseEntity<RiotAccountDto> findAccount(@PathVariable("gameName") String gameName
            , @PathVariable("tagLine") String tagLine) {
        try {
            RiotAccount account = riotAccountManager.findOrRegisterAccount(gameName, tagLine);
            return ResponseEntity.ok(RiotAccountDto.from(account));
        } catch (IllegalStateException e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/accounts/renew/{gameName}/{tagLine}")
    public ResponseEntity<RiotAccountDto> renewAccount(@PathVariable("gameName") String gameName
            , @PathVariable("tagLine") String tagLine) {
        try {
            RiotAccount account = riotAccountManager.renewAccount(gameName, tagLine);
            return ResponseEntity.ok(RiotAccountDto.from(account));
        } catch (IllegalStateException e) {
            log.error("Failed to renew account for {}#{}: {}", gameName, tagLine, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
