package com.glennsyj.rivals.api.rivalry.model;

import java.util.List;

public record RivalryCreationDto(
    List<RivalryParticipantDto> participants
) {
}