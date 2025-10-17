package ru.javabegin.oauth2.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenDto {
    @JsonProperty("access_token") public String accessToken;
    @JsonProperty("refresh_token") public String refreshToken;
    @JsonProperty("id_token") public String idToken;
    @JsonProperty("expires_in") public Integer expiresIn;
    @JsonProperty("refresh_expires_in") public Integer refreshExpiresIn;
    @JsonProperty("token_type") public String tokenType;
    @JsonProperty("scope") public String scope;
}
