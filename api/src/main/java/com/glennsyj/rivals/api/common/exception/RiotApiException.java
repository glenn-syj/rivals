package com.glennsyj.rivals.api.common.exception;

import org.springframework.http.HttpStatus;
import java.net.URI;

public class RiotApiException extends CustomException {

    private static final String TITLE = "Riot API Error";

    public RiotApiException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, TITLE, message, null);
    }

    public RiotApiException(String message, Integer riotApiStatusCode) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, TITLE, message, null);
        if (riotApiStatusCode != null) {
            getBody().setProperty("riotApiStatusCode", riotApiStatusCode);
        }
    }
} 