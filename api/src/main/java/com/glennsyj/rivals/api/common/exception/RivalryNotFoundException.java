package com.glennsyj.rivals.api.common.exception;

import org.springframework.http.HttpStatus;
import java.net.URI;

public class RivalryNotFoundException extends CustomException {

    private static final String TITLE = "Rivalry Not Found";

    public RivalryNotFoundException(Long rivalryId) {
        super(HttpStatus.NOT_FOUND, TITLE, "ID " + rivalryId + "에 해당하는 Rivalry를 찾을 수 없습니다.", null);
        getBody().setProperty("rivalryId", rivalryId);
    }

    public RivalryNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, TITLE, message, null);
    }
} 