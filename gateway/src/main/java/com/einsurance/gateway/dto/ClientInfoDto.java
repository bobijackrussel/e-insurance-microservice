package com.einsurance.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientInfoDto {
    private String clientId;
    private String clientSecret;
    private String redirectUrl;
}
