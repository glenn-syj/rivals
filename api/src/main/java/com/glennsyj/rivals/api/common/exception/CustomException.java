package com.glennsyj.rivals.api.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;
import java.net.URI;

public abstract class CustomException extends ErrorResponseException {

    protected CustomException(HttpStatus status, String title, String detail, URI type) {
        super(status, ProblemDetail.forStatusAndDetail(status, detail), null);
        getBody().setTitle(title);
        if (type != null) {
            getBody().setType(type);
        }
    }

    protected CustomException(HttpStatus status, ProblemDetail problemDetail) {
        super(status, problemDetail, null);
    }
} 