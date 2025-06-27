package com.glennsyj.rivals.api.common.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/*
    Riot API 내 도메인 별 API 호출을 위한 추상 클래스
 */
@Component
public abstract class BaseRiotClient {
    private static final Logger logger = LoggerFactory.getLogger(BaseRiotClient.class);
    private static final int MAX_RETRIES = 1;
    private static final Duration INITIAL_BACKOFF = Duration.ofMinutes(2);
    
    protected final WebClient riotAsiaWebClient;
    protected final WebClient riotKorWebClient;

    protected BaseRiotClient(WebClient riotAsiaWebClient, WebClient riotKorWebClient) {
        this.riotAsiaWebClient = riotAsiaWebClient;
        this.riotKorWebClient = riotKorWebClient;
    }

    protected <T> T handleApiCall(Mono<T> apiCall, String errorMessage) {
        long startTime = System.currentTimeMillis();
        try {
            return apiCall
                    .retryWhen(Retry.backoff(MAX_RETRIES, INITIAL_BACKOFF)
                            .filter(throwable -> shouldRetry(throwable))
                            .doBeforeRetry(retrySignal -> 
                                logger.warn("Retrying API call after error. Attempt {}/{}",
                                    retrySignal.totalRetries() + 1, MAX_RETRIES)))
                    .blockOptional()
                    .orElseThrow(() -> new IllegalStateException(errorMessage));
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                String retryAfter = e.getHeaders().getFirst("Retry-After");
                logger.error("Rate limit exceeded. Retry after {} seconds", retryAfter);
                throw new IllegalStateException("Riot API 호출 횟수 제한 초과. " + retryAfter + "초 후에 다시 시도해주세요.", e);
            }
            throw new IllegalStateException("Riot API 호출 실패: " + e.getResponseBodyAsString(), e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Riot API call for '{}' completed in {}ms", this.getClass(), duration);
        }
    }

    private boolean shouldRetry(Throwable throwable) {
        if (throwable instanceof WebClientResponseException e) {
            return e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS ||
                   e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE ||
                   e.getStatusCode() == HttpStatus.GATEWAY_TIMEOUT;
        }
        return false;
    }
}