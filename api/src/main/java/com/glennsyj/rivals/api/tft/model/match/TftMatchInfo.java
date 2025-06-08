package com.glennsyj.rivals.api.tft.model.match;

import java.util.List;

public record TftMatchInfo(
        String endOfGameResult,
        Long gameCreation,
        Long gameId,
        String game_variation,
        Long game_datetime,
        Double game_length,
        String game_version,
        Integer mapId,
        List<TftMatchParticipant> participants,
        Integer queueId,
        Integer queue_id,
        String tft_game_type,
        String tft_set_core_name,
        Integer tft_set_number
) {}
