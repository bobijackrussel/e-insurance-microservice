package com.einsurance.gateway.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class AuthServiceResult<T> {
    private final HttpHeaders headers;
    private final T body;
    private final HttpStatus status;
}
