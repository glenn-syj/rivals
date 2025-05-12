package com.glennsyj.rivals.api.riot;

import com.glennsyj.rivals.api.riot.model.RiotAccountResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class RiotApiClient {

    private WebClient riotAsiaWebClient;
    private WebClient riotKorWebClient;

    public RiotApiClient(WebClient riotAsiaWebClient, WebClient riotKorWebClient) {
        this.riotAsiaWebClient = riotAsiaWebClient;
        this.riotKorWebClient = riotKorWebClient;
    }

    public String getPUUID(String gameName, String tagLine) {
        try {
            return riotAsiaWebClient.get()
                    .uri("/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}",
                            gameName,
                            tagLine)
                    .retrieve()
                    .bodyToMono(RiotAccountResponse.class)
                    .blockOptional()
                    .map(RiotAccountResponse::puuid)
                    .orElseThrow(() -> new IllegalStateException("소환사를 찾을 수 없습니다: " + gameName + "#" + tagLine));
        } catch (WebClientResponseException e) {
            throw new IllegalStateException("Riot API 호출 실패: " + e.getMessage(), e);
        }
    }
}
