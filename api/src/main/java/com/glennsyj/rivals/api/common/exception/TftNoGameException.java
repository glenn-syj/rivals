package com.glennsyj.rivals.api.common.exception;

import org.springframework.http.HttpStatus;

public class TftNoGameException extends CustomException {
    private static final String TITLE = "Tft No Game Yet";

    public TftNoGameException(Long accountId) {
        super(HttpStatus.NOT_FOUND, TITLE, "ID " + accountId + "는 아직 TFT 게임을 진행하지 않았습니다.", null);
        getBody().setProperty("accountId", accountId);
    }

    public TftNoGameException(String message) {
        super(HttpStatus.NOT_FOUND, TITLE, message, null);
    }
}
