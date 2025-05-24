package com.glennsyj.rivals.api.rivalry.controller;

import com.glennsyj.rivals.api.rivalry.model.RivalryCreationDto;
import com.glennsyj.rivals.api.rivalry.model.RivalryResultDto;
import com.glennsyj.rivals.api.rivalry.service.RivalryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/rivalries")
public class RivalryController {

    private RivalryService rivalryService;

    public RivalryController(RivalryService rivalryService) {
        this.rivalryService = rivalryService;
    }

    @PostMapping("")
    public ResponseEntity<?> createRivalry(@Valid @RequestBody RivalryCreationDto creationDto) {

        Long rivalryId = rivalryService.createRivalryFrom(creationDto);
        RivalryResultDto response = new RivalryResultDto(rivalryId);

        URI uri = URI.create("/api/v1/rivalries/" + rivalryId);

        return ResponseEntity.created(uri).body(response);
    }
}
