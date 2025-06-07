package com.glennsyj.rivals.api.rivalry.model;

import com.glennsyj.rivals.api.tft.model.entry.TftStatusDto;

/*
    추후 확장될 가능성 있음
 */
public record ParticipantStatDto(
        String id,
        String fullName,
        TftStatusDto statistics
) {
}
