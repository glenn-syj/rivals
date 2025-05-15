package com.glennsyj.rivals.api.riot;

import com.glennsyj.rivals.api.common.client.BaseRiotClient;
import com.glennsyj.rivals.api.riot.model.RiotAccountResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class RiotAccountClient extends BaseRiotClient {

    public RiotAccountClient(WebClient riotAsiaWebClient, WebClient riotKorWebClient) {
        super(riotAsiaWebClient, riotKorWebClient);
    }

    public RiotAccountResponse getAccountInfo(String gameName, String tagLine) {
        return handleApiCall(
                riotAsiaWebClient.get()
                        .uri("/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}",
                                gameName, tagLine)
                        .retrieve()
                        .bodyToMono(RiotAccountResponse.class),
                "소환사를 찾을 수 없습니다: " + gameName + "#" + tagLine
        );
    }
}
