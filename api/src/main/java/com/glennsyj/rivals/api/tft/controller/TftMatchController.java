package com.glennsyj.rivals.api.tft.controller;

import com.glennsyj.rivals.api.tft.facade.TftFacade;
import com.glennsyj.rivals.api.tft.model.match.TftRecentMatchDto;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "TFT 매치 API", description = "TFT 매치와 관련된 작업")
@RestController
@RequestMapping("/api/v1/tft/matches")
public class TftMatchController {
    private final TftFacade tftFacade;

    public TftMatchController(TftFacade tftFacade) {
        this.tftFacade = tftFacade;
    }

    @Operation(summary = "최근 TFT 매치 조회",
            description = "주어진 게임명과 태그라인에 대한 최근 TFT 매치 목록을 검색합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "최근 매치 성공적으로 검색됨"),
                @ApiResponse(responseCode = "404", description = "Riot 계정 또는 매치를 찾을 수 없음"),
                @ApiResponse(responseCode = "500", description = "내부 서버 오류")
            })
    @GetMapping("/{gameName}/{tagLine}")
    public ResponseEntity<List<TftRecentMatchDto>> getRecentMatches(
            @PathVariable String gameName,
            @PathVariable String tagLine) {
        List<TftRecentMatchDto> dtos = tftFacade.findAndProcessMatches(gameName, tagLine);
        return ResponseEntity.ok(dtos);
    }
}
