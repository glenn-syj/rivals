package com.glennsyj.rivals.api.tft.model.match;

import java.util.List;

public record TftMatchUnit(
        String character_id,
        List<String> itemNames,
        String name,
        Integer rarity,
        Integer tier
) {}
