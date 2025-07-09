package com.glennsyj.rivals.api.rivalry.controller;

import com.glennsyj.rivals.api.rivalry.model.RivalryCreationDto;
import com.glennsyj.rivals.api.rivalry.model.RivalryDetailDto;
import com.glennsyj.rivals.api.rivalry.model.RivalryResultDto;
import com.glennsyj.rivals.api.rivalry.service.RivalryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.net.URI;

@Tag(name = "라이벌리 API", description = "라이벌리와 관련된 작업")
@RestController
@RequestMapping("/api/v1/rivalries")
public class RivalryController {

    private RivalryService rivalryService;

    public RivalryController(RivalryService rivalryService) {
        this.rivalryService = rivalryService;
    }

    @Operation(summary = "새로운 라이벌리 생성",
            description = "여러 Riot 계정 간에 새로운 라이벌리를 생성합니다.",
            responses = {
                @ApiResponse(responseCode = "201", description = "라이벌리 성공적으로 생성됨"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 본문"),
                @ApiResponse(responseCode = "500", description = "내부 서버 오류")
            })
    @PostMapping("")
    public ResponseEntity<?> createRivalry(@Valid @RequestBody RivalryCreationDto creationDto) {

        Long rivalryId = rivalryService.createRivalryFrom(creationDto);
        RivalryResultDto response = new RivalryResultDto(rivalryId.toString());

        URI uri = URI.create("/api/v1/rivalries/" + rivalryId);

        return ResponseEntity.created(uri).body(response);
    }

    @Operation(summary = "ID로 라이벌리 조회",
            description = "ID를 통해 특정 라이벌리의 상세 정보를 검색합니다.",
            responses = {
                @ApiResponse(responseCode = "200", description = "라이벌리 상세 정보 성공적으로 검색됨"),
                @ApiResponse(responseCode = "404", description = "라이벌리를 찾을 수 없음"),
                @ApiResponse(responseCode = "500", description = "내부 서버 오류")
            })
    @GetMapping("/{rivalryId}")
    public ResponseEntity<?> getRivalryById(@PathVariable String rivalryId) {

        RivalryDetailDto response = rivalryService.findRivalryFrom(Long.valueOf(rivalryId));
        return ResponseEntity.ok(response);
    }
}
