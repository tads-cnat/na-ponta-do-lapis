package com.npl.na_ponta_do_lapis.web.exception;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String error,
        String message,
        Map<String, String> errors
) {
    public ErrorResponse(String message) {
        this(message, message, null);
    }

    public ErrorResponse(String message, Map<String, String> errors) {
        this(message, message, errors);
    }
}
