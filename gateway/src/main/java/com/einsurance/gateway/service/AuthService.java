package com.einsurance.gateway.service;

import com.einsurance.gateway.dto.ClientInfoDto;
import com.einsurance.gateway.dto.LoginRedirectDto;
import com.einsurance.gateway.dto.TokenDto;
import com.einsurance.gateway.utils.CookieUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String STATE_COOKIE = "OAUTH_STATE";
    private static final String PKCE_COOKIE = "OAUTH_PKCE";
    private static final String ACCESS_TOKEN_COOKIE = "AT";
    private static final String REFRESH_TOKEN_COOKIE = "RT";
    private static final String ID_TOKEN_COOKIE = "IT";

    private final WebClient webClient;
    private final CookieUtils cookieUtils;
    private final ObjectMapper objectMapper;

    @Value("${keycloak.url}")
    private String keycloakBaseUrl;

    @Value("${keycloak.userclientid}")
    private String userClientId;

    @Value("${keycloak.usersecret}")
    private String userClientSecret;

    @Value("${keycloak.adminclientid}")
    private String adminClientId;

    @Value("${keycloak.adminsecret}")
    private String adminClientSecret;

    @Value("${clients.user}")
    private String userFrontendUrl;

    @Value("${clients.admin}")
    private String adminFrontendUrl;

    @Value("${gateway.callback-url:http://localhost:8903/api/v1/bff/callback}")
    private String callbackUrl;

    public Mono<AuthServiceResult<LoginRedirectDto>> initiateLogin(ServerWebExchange exchange, String clientParam) {
        ClientInfoDto clientInfo = resolveClient(exchange, clientParam);

        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        String state = generateState();

        URI loginUri = UriComponentsBuilder.fromHttpUrl(keycloakBaseUrl + "/auth")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientInfo.getClientId())
                .queryParam("scope", "openid profile email")
                .queryParam("redirect_uri", callbackUrl)
                .queryParam("state", state)
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .build(true)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(loginUri);
        headers.add(HttpHeaders.SET_COOKIE, cookieUtils.createCookie(STATE_COOKIE, state, 300).toString());
        headers.add(HttpHeaders.SET_COOKIE, cookieUtils.createCookie(PKCE_COOKIE, codeVerifier, 300).toString());

        LoginRedirectDto body = new LoginRedirectDto(loginUri.toString(), clientInfo.getClientId());
        return Mono.just(new AuthServiceResult<>(headers, body, HttpStatus.FOUND));
    }

    public Mono<AuthServiceResult<Void>> handleCallback(String code, String state, ServerWebExchange exchange) {
        String stateCookie = readCookie(exchange, STATE_COOKIE);
        if (stateCookie == null || !stateCookie.equals(state)) {
            return Mono.just(new AuthServiceResult<>(cookieUtils.clearCookiesHeaders(), null, HttpStatus.UNAUTHORIZED));
        }

        ClientInfoDto clientInfo = resolveClient(exchange, null);
        String codeVerifier = readCookie(exchange, PKCE_COOKIE);

        String tokenEndpoint = keycloakBaseUrl + "/token";
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("client_id", clientInfo.getClientId());
        form.add("client_secret", clientInfo.getClientSecret());
        form.add("redirect_uri", callbackUrl);
        if (codeVerifier != null && !codeVerifier.isBlank()) {
            form.add("code_verifier", codeVerifier);
        }

        return webClient.post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .exchangeToMono(response -> response.toEntity(String.class))
                .flatMap(entity -> {
                    if (!entity.getStatusCode().is2xxSuccessful()) {
                        return Mono.just(new AuthServiceResult<>(cookieUtils.clearCookiesHeaders(), null,
                                HttpStatus.UNAUTHORIZED));
                    }
                    try {
                        TokenDto tokenDto = objectMapper.readValue(entity.getBody(), TokenDto.class);
                        HttpHeaders headers = createTokenCookies(tokenDto);
                        headers.add(HttpHeaders.SET_COOKIE, cookieUtils.deleteCookie(STATE_COOKIE).toString());
                        headers.add(HttpHeaders.SET_COOKIE, cookieUtils.deleteCookie(PKCE_COOKIE).toString());
                        headers.setLocation(URI.create(clientInfo.getRedirectUrl()));
                        return Mono.just(new AuthServiceResult<>(headers, null, HttpStatus.FOUND));
                    } catch (Exception ex) {
                        return Mono.just(new AuthServiceResult<>(cookieUtils.clearCookiesHeaders(), null,
                                HttpStatus.INTERNAL_SERVER_ERROR));
                    }
                });
    }

    public Mono<AuthServiceResult<TokenDto>> refreshAccessToken(String refreshToken, ServerWebExchange exchange) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return Mono.just(new AuthServiceResult<>(cookieUtils.clearCookiesHeaders(), null, HttpStatus.UNAUTHORIZED));
        }

        ClientInfoDto clientInfo = resolveClient(exchange, null);
        String tokenEndpoint = keycloakBaseUrl + "/token";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);
        form.add("client_id", clientInfo.getClientId());
        form.add("client_secret", clientInfo.getClientSecret());

        return webClient.post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .exchangeToMono(response -> response.toEntity(String.class))
                .flatMap(entity -> {
                    if (!entity.getStatusCode().is2xxSuccessful()) {
                        return Mono.just(new AuthServiceResult<>(cookieUtils.clearCookiesHeaders(), null,
                                HttpStatus.UNAUTHORIZED));
                    }
                    try {
                        TokenDto tokenDto = objectMapper.readValue(entity.getBody(), TokenDto.class);
                        HttpHeaders headers = createTokenCookies(tokenDto);
                        return Mono.just(new AuthServiceResult<>(headers, tokenDto, HttpStatus.OK));
                    } catch (Exception ex) {
                        return Mono.just(new AuthServiceResult<>(cookieUtils.clearCookiesHeaders(), null,
                                HttpStatus.INTERNAL_SERVER_ERROR));
                    }
                });
    }

    public Mono<AuthServiceResult<Void>> logout(ServerWebExchange exchange, String idToken) {
        ClientInfoDto clientInfo = resolveClient(exchange, null);
        URI logoutUri = UriComponentsBuilder.fromHttpUrl(keycloakBaseUrl + "/logout")
                .queryParam("post_logout_redirect_uri", clientInfo.getRedirectUrl())
                .queryParam("id_token_hint", Optional.ofNullable(idToken).orElse(""))
                .queryParam("client_id", clientInfo.getClientId())
                .build(true)
                .toUri();

        return webClient.get()
                .uri(logoutUri)
                .retrieve()
                .toBodilessEntity()
                .map(responseEntity -> {
                    HttpHeaders headers = cookieUtils.clearCookiesHeaders();
                    headers.setLocation(URI.create(clientInfo.getRedirectUrl()));
                    AuthServiceResult<Void> result = new AuthServiceResult<>(headers, null, HttpStatus.FOUND);
                    return result;
                })
                .onErrorResume(ex -> {
                    HttpHeaders headers = cookieUtils.clearCookiesHeaders();
                    AuthServiceResult<Void> result = new AuthServiceResult<>(headers, null, HttpStatus.OK);
                    return Mono.just(result);
                });
    }

    private ClientInfoDto resolveClient(ServerWebExchange exchange, String clientParam) {
        String host = exchange.getRequest().getHeaders().getHost() != null
                ? exchange.getRequest().getHeaders().getHost().getHostName()
                : "";

        boolean adminClientRequested = "admin".equalsIgnoreCase(clientParam) || host.toLowerCase().contains("admin");
        if (adminClientRequested) {
            return new ClientInfoDto(adminClientId, adminClientSecret, adminFrontendUrl);
        }
        return new ClientInfoDto(userClientId, userClientSecret, userFrontendUrl);
    }

    private HttpHeaders createTokenCookies(TokenDto tokenDto) {
        HttpHeaders headers = new HttpHeaders();
        if (tokenDto.getAccessToken() != null) {
            headers.add(HttpHeaders.SET_COOKIE,
                    cookieUtils.createCookie(ACCESS_TOKEN_COOKIE, tokenDto.getAccessToken(),
                            Optional.ofNullable(tokenDto.getExpiresIn()).orElse(20)).toString());
        }
        if (tokenDto.getRefreshToken() != null) {
            headers.add(HttpHeaders.SET_COOKIE,
                    cookieUtils.createCookie(REFRESH_TOKEN_COOKIE, tokenDto.getRefreshToken(),
                            Optional.ofNullable(tokenDto.getRefreshExpiresIn()).orElse(3600)).toString());
        }
        if (tokenDto.getIdToken() != null) {
            headers.add(HttpHeaders.SET_COOKIE,
                    cookieUtils.createCookie(ID_TOKEN_COOKIE, tokenDto.getIdToken(),
                            Optional.ofNullable(tokenDto.getExpiresIn()).orElse(60)).toString());
        }
        return headers;
    }

    private String readCookie(ServerWebExchange exchange, String name) {
        return Optional.ofNullable(exchange.getRequest().getCookies().getFirst(name))
                .map(cookie -> cookie.getValue())
                .orElse(null);
    }

    private String generateCodeVerifier() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateCodeChallenge(String verifier) {
        try {
            byte[] sha = java.security.MessageDigest.getInstance("SHA-256")
                    .digest(verifier.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(sha);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate PKCE code challenge", e);
        }
    }

    private String generateState() {
        byte[] bytes = new byte[16];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
