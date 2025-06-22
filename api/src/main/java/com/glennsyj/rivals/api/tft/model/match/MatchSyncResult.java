package com.glennsyj.rivals.api.tft.model.match;

import com.glennsyj.rivals.api.tft.entity.match.TftMatch;
import java.time.LocalDateTime;
import java.util.List;

public record MatchSyncResult(
    List<TftMatch> allMatches,
    List<TftMatch> newMatches,
    boolean hasNewMatches,
    LocalDateTime lastSyncTime
) {
    public static MatchSyncResult of(List<TftMatch> allMatches, List<TftMatch> newMatches, LocalDateTime lastSyncTime) {
        return new MatchSyncResult(allMatches, newMatches, !newMatches.isEmpty(), lastSyncTime);
    }
} 