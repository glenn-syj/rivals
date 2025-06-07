package com.glennsyj.rivals.api.tft.model;

import java.util.List;

public record TftMatchMetadata(
        String data_version,
        String match_id,
        List<String> participants  // puuid 목록
) {}