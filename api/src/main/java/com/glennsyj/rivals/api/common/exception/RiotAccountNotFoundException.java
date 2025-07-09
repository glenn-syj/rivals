package com.glennsyj.rivals.api.common.exception;

import org.springframework.http.HttpStatus;
import java.net.URI;

public class RiotAccountNotFoundException extends CustomException {

    private static final String TITLE = "Riot Account Not Found";

    public RiotAccountNotFoundException(Long accountId) {
        super(HttpStatus.NOT_FOUND, TITLE, "ID " + accountId + "에 해당하는 Riot 계정을 찾을 수 없습니다.", null);
        getBody().setProperty("accountId", accountId);
    }

    public RiotAccountNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, TITLE, message, null);
    }
} 