package com.glennsyj.rivals.api.rivalry.model;

import com.glennsyj.rivals.api.rivalry.entity.RivalSide;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RivalryCreationDto(
        @NotEmpty(message="participants should not be empty")
        List<RivalryParticipantDto> participants
) {
    public RivalryCreationDto {

        // LEFT SIDE와 RIGHT SIDE에 최소한 하나의 참여자는 있도록 함
        boolean hasLeft = participants.stream()
                .anyMatch(p -> p.side() == RivalSide.LEFT);
        boolean hasRight = participants.stream()
                .anyMatch(p -> p.side() == RivalSide.RIGHT);

        if (!hasLeft || !hasRight) {
            throw new IllegalArgumentException(
                    "At least one participant required for each side (LEFT and RIGHT)");
        }
    }
}