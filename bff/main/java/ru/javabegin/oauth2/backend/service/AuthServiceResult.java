package ru.javabegin.oauth2.backend.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public class AuthServiceResult<T> {
    private final HttpHeaders headers;
    private final T body;
    private final HttpStatus status;
}
