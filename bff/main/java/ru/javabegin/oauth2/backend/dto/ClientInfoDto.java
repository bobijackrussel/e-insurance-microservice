package ru.javabegin.oauth2.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ClientInfoDto {
    private String clientId;
    private String clientSecret;
    private String redirectURL;
}
