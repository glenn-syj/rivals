package com.glennsyj.rivals.api.tft.model.badge;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record TftBadgeBulkRequestDto(
    @NotEmpty(message = "PUUIDs cannot be empty")
    List<String> puuids
) {
}
