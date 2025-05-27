package com.glennsyj.rivals.api.rivalry.controller;

import com.glennsyj.rivals.api.rivalry.model.RivalryCreationDto;
import com.glennsyj.rivals.api.rivalry.model.RivalryDetailDto;
import com.glennsyj.rivals.api.rivalry.model.RivalryResultDto;
import com.glennsyj.rivals.api.rivalry.service.RivalryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        System.out.println("creationDto: " + creationDto);
        Long rivalryId = rivalryService.createRivalryFrom(creationDto);
        RivalryResultDto response = new RivalryResultDto(rivalryId.toString());

        URI uri = URI.create("/api/v1/rivalries/" + rivalryId);

        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping("/{rivalryId}")
    public ResponseEntity<?> getRivalryById(@PathVariable String rivalryId) {

        try {
            RivalryDetailDto response = rivalryService.findRivalryFrom(Long.valueOf(rivalryId));
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
