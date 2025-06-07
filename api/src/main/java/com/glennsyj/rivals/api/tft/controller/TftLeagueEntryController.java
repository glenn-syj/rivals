package com.glennsyj.rivals.api.tft.controller;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.service.RiotAccountManager;
import com.glennsyj.rivals.api.tft.entity.entry.TftLeagueEntry;
import com.glennsyj.rivals.api.tft.model.entry.TftStatusDto;
import com.glennsyj.rivals.api.tft.service.TftLeagueEntryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path="/api/v1/tft/entries")
public class TftLeagueEntryController {

    private TftLeagueEntryManager tftLeagueEntryManager;
    private RiotAccountManager riotAccountManager;

    private final Logger log = LoggerFactory.getLogger(TftLeagueEntryController.class);

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
            List<TftLeagueEntry> entries = tftLeagueEntryManager.findOrCreateLeagueEntries(account.getId());

            List<TftStatusDto> dtos = new ArrayList<>(entries.size());
            for (TftLeagueEntry entry : entries) {
                TftStatusDto dto = TftStatusDto.from(entry);
                dtos.add(dto);
            }

            return ResponseEntity.ok(dtos);
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(path="/{gameName}/{tagLine}")
    public ResponseEntity<?> getTftStatusFrom(@PathVariable String gameName, @PathVariable String tagLine) {
        try {
            RiotAccount account = riotAccountManager.findOrRegisterAccount(gameName, tagLine);
            List<TftLeagueEntry> entries = tftLeagueEntryManager.findOrCreateLeagueEntries(account.getId());

            List<TftStatusDto> dtos = new ArrayList<>(entries.size());
            for (TftLeagueEntry entry : entries) {
                TftStatusDto dto = TftStatusDto.from(entry);
                dtos.add(dto);
            }

            return ResponseEntity.ok(dtos);
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
