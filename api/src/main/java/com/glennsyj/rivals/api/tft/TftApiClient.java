package com.glennsyj.rivals.api.tft;

import com.glennsyj.rivals.api.common.client.BaseRiotClient;
import com.glennsyj.rivals.api.tft.model.TftLeagueEntryResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

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
}
