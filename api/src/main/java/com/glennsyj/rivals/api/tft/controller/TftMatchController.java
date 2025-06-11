package com.glennsyj.rivals.api.tft.controller;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.service.RiotAccountManager;
import com.glennsyj.rivals.api.tft.entity.match.TftMatch;
import com.glennsyj.rivals.api.tft.model.match.TftRecentMatchDto;
import com.glennsyj.rivals.api.tft.service.TftMatchManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path="/api/v1/tft/matches")
public class TftMatchController {

    private TftMatchManager tftMatchManager;
    private RiotAccountManager riotAccountManager;

    public TftMatchController(TftMatchManager tftMatchManager, RiotAccountManager riotAccountManager) {
        this.tftMatchManager = tftMatchManager;
        this.riotAccountManager = riotAccountManager;
    }

    @GetMapping(path="/{gameName}/{tagLine}")
    public ResponseEntity<List<TftRecentMatchDto>> findOrCreateRecentTftMatches(@PathVariable String gameName, @PathVariable String tagLine) {
        try {
            RiotAccount account = riotAccountManager.findOrRegisterAccount(gameName, tagLine);
            List<TftMatch> matches = tftMatchManager.findOrCreateRecentTftMatches(account.getId(), account.getPuuid());

            List<TftRecentMatchDto> matchDtos = matches.stream()
                    .map((match) -> TftRecentMatchDto.from(account.getPuuid(), match)).toList();

            return ResponseEntity.ok(matchDtos);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

}
