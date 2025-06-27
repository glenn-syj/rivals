package com.glennsyj.rivals.api.tft;

import com.glennsyj.rivals.api.common.client.BaseRiotClient;
import com.glennsyj.rivals.api.tft.model.entry.TftLeagueEntryResponse;
import com.glennsyj.rivals.api.tft.model.match.TftMatchResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class TftApiClient extends BaseRiotClient {
    public TftApiClient(WebClient riotAsiaWebClient, WebClient riotKorWebClient) {
        super(riotAsiaWebClient, riotKorWebClient);
    }

    public List<TftLeagueEntryResponse> getLeagueEntries(String puuid) {
        return handleApiCall(
                riotKorWebClient.get()
                        .uri("/tft/league/v1/by-puuid/{puuid}", puuid)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<TftLeagueEntryResponse>>() {
                        }),
                "소환사의 TFT 리그 정보를 찾을 수 없습니다: " + puuid
        );
    }

    // 현재는 따로 Query Param 이용 없이 기본 값으로 이용 (20개)
    public List<String> getMatchIdsFromPuuid(String puuid) {
        return handleApiCall(
                riotAsiaWebClient.get()
                        .uri("/tft/match/v1/matches/by-puuid/{puuid}/ids", puuid)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<String>>() {
                        }),
                "소환사의 TFT 매치 ID 정보를 찾을 수 없습니다: " + puuid
        );
    }

    public TftMatchResponse getMatchResponseFromMatchId(String matchId) {
        return handleApiCall(
                riotAsiaWebClient.get()
                        .uri("/tft/match/v1/matches/{matchId}", matchId)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(TftMatchResponse.class),
                "매치 ID 기반 매치 정보를 찾을 수 없습니다: " + matchId
        );
    }

    public Mono<TftMatchResponse> getMatchResponseFromMatchIdMono(String matchId) {
        return riotAsiaWebClient.get()
            .uri("/tft/match/v1/matches/{matchId}", matchId)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(TftMatchResponse.class);
    }
}
