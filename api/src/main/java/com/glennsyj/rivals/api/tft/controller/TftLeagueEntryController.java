package com.glennsyj.rivals.api.tft.controller;

import com.glennsyj.rivals.api.common.exception.RiotAccountNotFoundException;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "TFT 리그 엔트리 API", description = "TFT 리그 엔트리와 관련된 작업")
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

    @Operation(summary = "인코딩된 전체 이름으로 TFT 리그 상태 조회",
            description = "주어진 인코딩된 전체 이름(게임명#태그라인)에 대한 TFT 리그 엔트리 상태를 검색합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "TFT 리그 엔트리 성공적으로 검색됨"),
                @ApiResponse(responseCode = "400", description = "잘못된 인코딩된 전체 이름 형식"),
                @ApiResponse(responseCode = "404", description = "Riot 계정 또는 TFT 리그 엔트리를 찾을 수 없음"),
                @ApiResponse(responseCode = "500", description = "내부 서버 오류")
            })
    @GetMapping(path="/{encodedFullName}")
    public ResponseEntity<?> getTftStatusFromFullName(@PathVariable String encodedFullName) {
        // '#' character는 PathVariable로 전달되지 않음.
        String[] parts = encodedFullName.split("#");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid format for encodedFullName: " + encodedFullName);
        }

        String gameName = parts[0];
        String tagLine = parts[1];

        RiotAccount account = riotAccountManager.findAccount(gameName, tagLine)
                .orElseThrow(() -> new RiotAccountNotFoundException(gameName, tagLine));
        List<TftLeagueEntry> entries = tftLeagueEntryManager.findOrCreateLeagueEntries(account.getId());

        List<TftStatusDto> dtos = new ArrayList<>(entries.size());
        for (TftLeagueEntry entry : entries) {
            TftStatusDto dto = TftStatusDto.from(entry);
            dtos.add(dto);
        }

        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "게임명 및 태그라인으로 TFT 리그 상태 조회",
            description = "주어진 게임명과 태그라인에 대한 TFT 리그 엔트리 상태를 검색합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "TFT 리그 엔트리 성공적으로 검색됨"),
                @ApiResponse(responseCode = "404", description = "Riot 계정 또는 TFT 리그 엔트리를 찾을 수 없음"),
                @ApiResponse(responseCode = "500", description = "내부 서버 오류")
            })
    @GetMapping(path="/{gameName}/{tagLine}")
    public ResponseEntity<?> getTftStatusFrom(@PathVariable String gameName, @PathVariable String tagLine) {
        RiotAccount account = riotAccountManager.findAccount(gameName, tagLine)
                .orElseThrow(() -> new RiotAccountNotFoundException(gameName, tagLine));
        List<TftLeagueEntry> entries = tftLeagueEntryManager.findOrCreateLeagueEntries(account.getId());

        List<TftStatusDto> dtos = new ArrayList<>(entries.size());
        for (TftLeagueEntry entry : entries) {
            TftStatusDto dto = TftStatusDto.from(entry);
            dtos.add(dto);
        }

        return ResponseEntity.ok(dtos);
    }
}
