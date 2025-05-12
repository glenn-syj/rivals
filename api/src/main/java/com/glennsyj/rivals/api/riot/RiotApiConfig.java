package com.glennsyj.rivals.api.riot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class RiotApiConfig {

    @Value("${riot.api.key}")
    private String apiKey;

    @Value("${riot.api.asia-url}")
    private String riotAsiaUrl;

    @Value("${riot.api.kor-url}")
    private String riotKorUrl;

    @Bean
    public WebClient riotAsiaWebClient() {
        return WebClient.builder()
                .baseUrl(riotAsiaUrl)
                .defaultHeader("X-Riot-Token", apiKey)
                .build();
    }

    @Bean
    public WebClient riotKorWebClient() {
        return WebClient.builder()
                .baseUrl(riotKorUrl)
                .defaultHeader("X-Riot-Token", apiKey)
                .build();
    }

}
