package com.glennsyj.rivals.api.rivalry.model;

import com.glennsyj.rivals.api.rivalry.entity.RivalryParticipant;
import com.glennsyj.rivals.api.rivalry.entity.RivalSide;
import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "라이벌리 참가자 정보 DTO")
public record RivalryParticipantDto(
    @Schema(description = "참가자 Riot 계정 ID")
    String id,
    @Schema(description = "참가자 사이드 (LEFT 또는 RIGHT)")
    RivalSide side) {

    public static RivalryParticipantDto from(RivalryParticipant participant) {
        return new RivalryParticipantDto(
                Long.toString(participant.getRiotAccount().getId()),
                participant.getSide()
        );
    }
}