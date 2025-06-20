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

@RestController
@RequestMapping("/api/v1/tft/renew")
public class TftRenewController {
    private final TftFacade tftFacade;
    private final Logger log = LoggerFactory.getLogger(TftRenewController.class);

    public TftRenewController(TftFacade tftFacade) {
        this.tftFacade = tftFacade;
    }

    @GetMapping("/{gameName}/{tagLine}")
    public ResponseEntity<TftRenewDto> renewAll(
            @PathVariable String gameName,
            @PathVariable String tagLine
    ) {
        try {
            TftRenewDto result = tftFacade.renewAllTftData(gameName, tagLine);
            return ResponseEntity.ok(result);
        } catch (TftRenewException e) {
            log.error("Failed to renew TFT data for {}#{}", gameName, tagLine, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
