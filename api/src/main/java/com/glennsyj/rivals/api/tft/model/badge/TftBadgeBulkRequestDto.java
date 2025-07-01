package com.glennsyj.rivals.api.tft.model.badge;

public record TftBadgeBulkRequestDto(
    List<PlayerIdentifierDto> players
) {
}
