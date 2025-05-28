package com.glennsyj.rivals.api.riot.controller;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.model.RiotAccountResponse;
import com.glennsyj.rivals.api.riot.service.RiotAccountManager;
import com.glennsyj.rivals.api.tft.service.TftLeagueEntryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/riot")
public class RiotController {

    private final RiotAccountManager riotAccountManager;
    private final TftLeagueEntryManager tftLeagueEntryManager;

    private final Logger log = LoggerFactory.getLogger(RiotController.class);

    public RiotController(RiotAccountManager riotAccountManager, TftLeagueEntryManager tftLeagueEntryManager) {

        this.riotAccountManager = riotAccountManager;
        this.tftLeagueEntryManager = tftLeagueEntryManager;
    }

    @GetMapping("/accounts/{gameName}/{tagLine}")
    public ResponseEntity<?> findAccount(@PathVariable("gameName") String gameName
            , @PathVariable("tagLine") String tagLine) {

        try {
            RiotAccount account = riotAccountManager.findOrRegisterAccount(gameName, tagLine);
            RiotAccountResponse response = new RiotAccountResponse(account.getPuuid(),
                account.getGameName(),
                account.getTagLine(),
                String.valueOf(account.getId()));

            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }

    }

    @GetMapping("/accounts/renew/{gameName}/{tagLine}")
    public ResponseEntity<?> renewAccount(@PathVariable("gameName") String gameName
            , @PathVariable("tagLine") String tagLine) {

        try {
            RiotAccount account = riotAccountManager.renewAccount(gameName, tagLine);
            RiotAccountResponse response = new RiotAccountResponse(account.getPuuid(),
                    account.getGameName(),
                    account.getTagLine(),
                    String.valueOf(account.getId()));

            // 응답에는 포함되지 않더라도 갱신 요청 DB에 반영 이후 새로고침으로 해결 예정
            tftLeagueEntryManager.renewEntry(account.getPuuid());

            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }

    }

}
