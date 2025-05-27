package com.glennsyj.rivals.api.tft.controller;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.service.RiotAccountManager;
import com.glennsyj.rivals.api.tft.entity.TftLeagueEntry;
import com.glennsyj.rivals.api.tft.model.TftStatusDto;
import com.glennsyj.rivals.api.tft.service.TftLeagueEntryManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping(path="/api/v1/tft/entries")
public class TftLeagueEntryController {

    private TftLeagueEntryManager tftLeagueEntryManager;
    private RiotAccountManager riotAccountManager;

    public TftLeagueEntryController(TftLeagueEntryManager tftLeagueEntryManager, RiotAccountManager riotAccountManager) {
        this.tftLeagueEntryManager = tftLeagueEntryManager;
        this.riotAccountManager = riotAccountManager;
    }

    @GetMapping(path="/{encodedFullName}")
    public ResponseEntity<?> getTftStatusFromFullName(@PathVariable String encodedFullName) {
        // '#' character는 PathVariable로 전달되지 않음.
        String[] parts = encodedFullName.split("#");
        if (parts.length != 2) {
            return ResponseEntity.badRequest().body("Invalid format for encodedFullName: " + encodedFullName);
        }

        String gameName = parts[0];
        String tagLine = parts[1];

        try {
            RiotAccount account = riotAccountManager.findOrRegisterAccount(gameName, tagLine);
            TftLeagueEntry entry = tftLeagueEntryManager.findOrCreateEntry(account.getId());

            TftStatusDto dto = TftStatusDto.from(entry);

            return ResponseEntity.ok(dto);
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(path="/{gameName}/{tagLine}")
    public ResponseEntity<?> getTftStatusFrom(@PathVariable String gameName, @PathVariable String tagLine) {
        try {
            RiotAccount account = riotAccountManager.findOrRegisterAccount(gameName, tagLine);
            TftLeagueEntry entry = tftLeagueEntryManager.findOrCreateEntry(account.getId());

            TftStatusDto dto = TftStatusDto.from(entry);

            return ResponseEntity.ok(dto);
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
