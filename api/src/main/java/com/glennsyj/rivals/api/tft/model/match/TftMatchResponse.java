package com.glennsyj.rivals.api.tft.model.match;

public record TftMatchResponse(
        TftMatchMetadata metadata,
        TftMatchInfo info
) {}
