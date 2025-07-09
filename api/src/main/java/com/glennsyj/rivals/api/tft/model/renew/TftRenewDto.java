package com.glennsyj.rivals.api.tft.model.renew;

import com.glennsyj.rivals.api.tft.model.badge.TftBadgeDto;
import com.glennsyj.rivals.api.tft.model.entry.TftStatusDto;
import com.glennsyj.rivals.api.tft.model.match.TftRecentMatchDto;

import java.time.LocalDateTime;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "TFT 데이터 갱신 응답 DTO")
public record TftRenewDto(
    @Schema(description = "갱신된 TFT 리그 엔트리 상태 목록")
    List<TftStatusDto> statuses,
    @Schema(description = "갱신된 최근 TFT 매치 목록")
    List<TftRecentMatchDto> matches,
    @Schema(description = "갱신된 TFT 배지 목록")
    List<TftBadgeDto> badges,
    @Schema(description = "갱신 시간")
    LocalDateTime renewedAt
) { }
