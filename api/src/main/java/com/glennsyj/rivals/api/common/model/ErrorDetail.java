package com.glennsyj.rivals.api.common.model;

public record ErrorDetail (
    String field,
    String message
){ }
