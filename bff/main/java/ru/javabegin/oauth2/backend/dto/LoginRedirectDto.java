package ru.javabegin.oauth2.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginRedirectDto {
    private String loginURL;
    private String clientId;
}
