package com.glennsyj.rivals.api.tft.controller;

import com.glennsyj.rivals.api.tft.facade.TftFacade;
import com.glennsyj.rivals.api.tft.facade.exception.TftRenewException;
import com.glennsyj.rivals.api.tft.model.renew.TftRenewDto;
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

@Tag(name = "TFT 갱신 API", description = "TFT 데이터 갱신과 관련된 작업")
@RestController
@RequestMapping("/api/v1/tft/renew")
public class TftRenewController {
    private final TftFacade tftFacade;
    private final Logger log = LoggerFactory.getLogger(TftRenewController.class);

    public TftRenewController(TftFacade tftFacade) {
        this.tftFacade = tftFacade;
    }

    @Operation(summary = "모든 TFT 데이터 갱신",
            description = "주어진 게임명과 태그라인에 대한 모든 TFT 관련 데이터(예: 리그 엔트리, 매치)를 갱신합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "모든 TFT 데이터 성공적으로 갱신됨"),
                @ApiResponse(responseCode = "404", description = "Riot 계정 혹은 다른 Riot 리소스를 찾을 수 없음"),
                @ApiResponse(responseCode = "500", description = "내부 서버 오류 또는 TFT 갱신 실패")
            })
    @GetMapping("/{gameName}/{tagLine}")
    public ResponseEntity<TftRenewDto> renewAll(
            @PathVariable String gameName,
            @PathVariable String tagLine
    ) {
        TftRenewDto result = tftFacade.renewAllTftData(gameName, tagLine);
        return ResponseEntity.ok(result);
    }
}
