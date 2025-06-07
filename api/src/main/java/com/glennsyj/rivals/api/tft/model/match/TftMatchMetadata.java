package com.glennsyj.rivals.api.tft.model.match;

import java.util.List;

public record TftMatchMetadata(
        String data_version,
        String match_id,
        List<String> participants  // puuid 목록
) {}