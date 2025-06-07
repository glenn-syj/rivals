package com.glennsyj.rivals.api.tft.model;

import java.util.List;

public record TftMatchUnit(
        String character_id,
        List<String> itemNames,
        String name,
        Integer rarity,
        Integer tier
) {}
