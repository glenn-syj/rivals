package com.glennsyj.rivals.api.tft.model.renew;

import com.glennsyj.rivals.api.tft.model.badge.TftBadgeDto;
import com.glennsyj.rivals.api.tft.model.entry.TftStatusDto;
import com.glennsyj.rivals.api.tft.model.match.TftRecentMatchDto;

import java.time.LocalDateTime;
import java.util.List;

public record TftRenewDto(
    List<TftStatusDto> statuses,
    List<TftRecentMatchDto> matches,
    List<TftBadgeDto> badges,
    LocalDateTime renewedAt
) { }
