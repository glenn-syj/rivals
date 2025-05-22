package com.glennsyj.rivals.api.riot.controller;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.model.RiotAccountResponse;
import com.glennsyj.rivals.api.riot.service.RiotAccountManager;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/riot")
public class RiotController {

    private final RiotAccountManager riotAccountManager;

    public RiotController(RiotAccountManager riotAccountManager) {
        this.riotAccountManager = riotAccountManager;
    }

    @GetMapping("/accounts/{gameName}/{tagLine}")
    public ResponseEntity<?> findAccount(@PathVariable("gameName") String gameName
            , @PathVariable("tagLine") String tagLine) {

        try {
            RiotAccount account = riotAccountManager.findOrRegisterAccount(gameName, tagLine);
            RiotAccountResponse response = new RiotAccountResponse(account.getPuuid(),
                account.getGameName(),
                account.getTagLine());

            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
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
                    account.getTagLine());

            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }

    }

}
