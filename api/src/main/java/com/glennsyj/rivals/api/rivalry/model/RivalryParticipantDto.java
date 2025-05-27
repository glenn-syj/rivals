package com.glennsyj.rivals.api.rivalry.model;

import com.glennsyj.rivals.api.rivalry.entity.RivalryParticipant;
import com.glennsyj.rivals.api.rivalry.entity.RivalSide;


public record RivalryParticipantDto(
    String id,
    RivalSide side) {

    public static RivalryParticipantDto from(RivalryParticipant participant) {
        return new RivalryParticipantDto(
                Long.toString(participant.getRiotAccount().getId()),
                participant.getSide()
        );
    }
}