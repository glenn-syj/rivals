package com.glennsyj.rivals.api.tft.model;

public record TftMatchResponse(
        TftMatchMetadata metadata,
        TftMatchInfo info
) {}
