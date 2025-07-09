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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Riot API", description = "Riot 계정과 관련된 작업")
@RestController
@RequestMapping("/api/v1/riot")
public class RiotController {

    private final RiotAccountManager riotAccountManager;

    private final Logger log = LoggerFactory.getLogger(RiotController.class);

    public RiotController(RiotAccountManager riotAccountManager) {
        this.riotAccountManager = riotAccountManager;
    }

    @Operation(summary = "Riot 계정 찾기 또는 등록",
            description = "기존 Riot 계정을 검색하거나, 없는 경우 새로 등록합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "계정 성공적으로 찾음 또는 등록됨"),
                @ApiResponse(responseCode = "404", description = "Riot 계정을 찾을 수 없음"),
                @ApiResponse(responseCode = "500", description = "내부 서버 오류")
            })
    @GetMapping("/accounts/{gameName}/{tagLine}")
    public ResponseEntity<RiotAccountDto> findAccount(@PathVariable("gameName") String gameName
            , @PathVariable("tagLine") String tagLine) {
        RiotAccount account = riotAccountManager.findOrRegisterAccount(gameName, tagLine);
        return ResponseEntity.ok(RiotAccountDto.from(account));
    }

    @Operation(summary = "Riot 계정 갱신",
            description = "기존 Riot 계정 정보를 갱신합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "계정 성공적으로 갱신됨"),
                @ApiResponse(responseCode = "404", description = "Riot 계정을 찾을 수 없음"),
                @ApiResponse(responseCode = "500", description = "내부 서버 오류")
            })
    @PatchMapping("/accounts/renew/{gameName}/{tagLine}")
    public ResponseEntity<RiotAccountDto> renewAccount(@PathVariable("gameName") String gameName
            , @PathVariable("tagLine") String tagLine) {
        RiotAccount account = riotAccountManager.renewAccount(gameName, tagLine);
        return ResponseEntity.ok(RiotAccountDto.from(account));
    }
}
