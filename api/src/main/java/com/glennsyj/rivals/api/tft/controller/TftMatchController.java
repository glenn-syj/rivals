package com.glennsyj.rivals.api.tft.controller;

import com.glennsyj.rivals.api.tft.facade.TftFacade;
import com.glennsyj.rivals.api.tft.model.match.TftRecentMatchDto;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tft/matches")
public class TftMatchController {
    private final TftFacade tftFacade;

    public TftMatchController(TftFacade tftFacade) {
        this.tftFacade = tftFacade;
    }

    @GetMapping("/{gameName}/{tagLine}")
    public ResponseEntity<List<TftRecentMatchDto>> getRecentMatches(
            @PathVariable String gameName,
            @PathVariable String tagLine) {
        try {
            List<TftRecentMatchDto> dtos = tftFacade.findAndProcessMatches(gameName, tagLine);
            return ResponseEntity.ok(dtos);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
