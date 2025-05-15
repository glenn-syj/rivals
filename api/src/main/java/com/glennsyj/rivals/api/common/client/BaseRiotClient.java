package com.glennsyj.rivals.api.common.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/*
    Riot API 내 도메인 별 API 호출을 위한 추상 클래스
 */
@Component
public abstract class BaseRiotClient {
    protected final WebClient riotAsiaWebClient;
    protected final WebClient riotKorWebClient;

    protected BaseRiotClient(WebClient riotAsiaWebClient, WebClient riotKorWebClient) {
        this.riotAsiaWebClient = riotAsiaWebClient;
        this.riotKorWebClient = riotKorWebClient;
    }

    protected <T> T handleApiCall(Mono<T> apiCall, String errorMessage) {
        try {
            return apiCall
                    .blockOptional()
                    .orElseThrow(() -> new IllegalStateException(errorMessage));
        } catch (WebClientResponseException e) {
            throw new IllegalStateException("Riot API 호출 실패: " + e.getResponseBodyAsString(), e);
        }
    }
}