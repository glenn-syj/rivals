package com.glennsyj.rivals.api.rivalry.model;

import java.time.LocalDateTime;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "라이벌리 상세 정보 DTO")
public record RivalryDetailDto(
        @Schema(description = "라이벌리 ID")
        String rivalryId,
        @Schema(description = "LEFT 팀 참가자 통계 목록")
        List<ParticipantStatDto> leftStats,
        @Schema(description = "RIGHT 팀 참가자 통계 목록")
        List<ParticipantStatDto> rightStats,
        @Schema(description = "라이벌리 생성 시간")
        LocalDateTime createdAt
) {
}
