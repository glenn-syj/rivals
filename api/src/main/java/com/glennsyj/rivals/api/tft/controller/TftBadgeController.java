package com.glennsyj.rivals.api.tft.controller;

import com.glennsyj.rivals.api.riot.entity.RiotAccount;
import com.glennsyj.rivals.api.riot.service.RiotAccountManager;
import com.glennsyj.rivals.api.tft.entity.achievement.TftBadgeProgress;
import com.glennsyj.rivals.api.tft.model.badge.TftBadgeBulkRequestDto;
import com.glennsyj.rivals.api.tft.model.badge.TftBadgeBulkResponseDto;
import com.glennsyj.rivals.api.tft.model.badge.TftBadgeDto;
import com.glennsyj.rivals.api.tft.service.TftBadgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "TFT 뱃지 API", description = "TFT 뱃지 및 업적과 관련된 작업")
@RestController
@RequestMapping("/api/v1/tft/badges")
public class TftBadgeController {

    private final TftBadgeService tftBadgeService;
    private final RiotAccountManager riotAccountManager;

    private final Logger log = LoggerFactory.getLogger(TftBadgeController.class);

    public TftBadgeController(TftBadgeService tftBadgeService, RiotAccountManager riotAccountManager) {
        this.tftBadgeService = tftBadgeService;
        this.riotAccountManager = riotAccountManager;
    }

    @Operation(summary = "소환사의 모든 TFT 뱃지 조회",
            description = "주어진 게임명과 태그라인에 대한 모든 TFT 뱃지 및 진행 상황을 검색합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "뱃지 성공적으로 검색됨"),
                @ApiResponse(responseCode = "404", description = "Riot 계정을 찾을 수 없음"),
                @ApiResponse(responseCode = "500", description = "내부 서버 오류")
            })
    @GetMapping("/{gameName}/{tagLine}")
    public ResponseEntity<List<TftBadgeDto>> findAllBadges(
            @PathVariable String gameName,
            @PathVariable String tagLine) {
        List<TftBadgeDto> badges = tftBadgeService.findAllBadges(gameName, tagLine);
        return ResponseEntity.ok(badges);
    }

    @Operation(summary = "소환사의 TFT 뱃지 초기화 또는 조회",
            description = "소환사의 뱃지가 존재하지 않으면 초기화하고, 존재하면 뱃지를 검색합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "뱃지 성공적으로 초기화 또는 검색됨"),
                @ApiResponse(responseCode = "404", description = "Riot 계정을 찾을 수 없음"),
                @ApiResponse(responseCode = "500", description = "내부 서버 오류")
            })
    @GetMapping("/{gameName}/{tagLine}/initialize")
    public ResponseEntity<List<TftBadgeDto>> initializeOrGetBadgesForSummoner(
            @PathVariable String gameName,
            @PathVariable String tagLine) {
        RiotAccount account = riotAccountManager.findOrRegisterAccount(gameName, tagLine);
        List<TftBadgeDto> badges = tftBadgeService.findAllBadges(account);
        return ResponseEntity.ok(badges);
    }

    @Operation(summary = "특정 TFT 뱃지 조회",
            description = "게임명, 태그라인, 뱃지 유형으로 특정 TFT 뱃지를 검색합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "뱃지 성공적으로 검색됨"),
                @ApiResponse(responseCode = "404", description = "Riot 계정 또는 뱃지를 찾을 수 없음"),
                @ApiResponse(responseCode = "500", description = "내부 서버 오류")
            })
    @GetMapping("/{gameName}/{tagLine}/{badgeType}")
    public ResponseEntity<TftBadgeDto> findBadge(
            @PathVariable String gameName,
            @PathVariable String tagLine,
            @PathVariable String badgeType) {
        RiotAccount account = riotAccountManager.findOrRegisterAccount(gameName, tagLine);
        return tftBadgeService.findBadge(account, TftBadgeProgress.BadgeType.valueOf(badgeType))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "PUUID로 TFT 뱃지 대량 조회",
            description = "여러 소환사의 PUUID를 기반으로 TFT 뱃지를 검색합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "PUUID에 대한 뱃지 성공적으로 검색됨"),
                @ApiResponse(responseCode = "500", description = "내부 서버 오류")
            })
    @PostMapping("/bulk")
    public ResponseEntity<TftBadgeBulkResponseDto> findBadgeBulkWithPuuids(@RequestBody TftBadgeBulkRequestDto requestDto) {

        TftBadgeBulkResponseDto responseDto = tftBadgeService.findBadgesFromPuuids(requestDto.puuids());
        return ResponseEntity.ok(responseDto);
    }
} 