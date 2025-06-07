package com.glennsyj.rivals.api.tft.model.match;

import java.util.List;
import java.util.Map;

/*
    변수 명은 riot API 응답과 동일하게 작성
 */
public record TftMatchParticipant(
        TftMatchCompanion companion,
        Integer gold_left,
        Integer last_round,
        Integer level,
        Map<String, Integer> missions,
        Integer placement,
        Integer players_eliminated,
        String puuid,
        String riotIdGameName,
        String riotIdTagline,
        Double time_eliminated,
        Integer total_damage_to_players,
        List<TftMatchTrait> traits,
        List<TftMatchUnit> units,
        Boolean win
) {}
