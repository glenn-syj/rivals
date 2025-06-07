package com.glennsyj.rivals.api.tft.model;

public record TftMatchTrait(
        String name,
        Integer num_units,
        Integer style,
        Integer tier_current,
        Integer tier_total
) {}
