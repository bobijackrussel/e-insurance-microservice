package ru.javabegin.oauth2.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.pqc.jcajce.provider.SABER;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import ru.javabegin.oauth2.backend.dto.ClientInfoDto;
import ru.javabegin.oauth2.backend.dto.LoginRedirectDto;
import ru.javabegin.oauth2.backend.dto.TokenDto;
import ru.javabegin.oauth2.backend.utils.CookieUtils;

import java.net.URI;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService{

    private final WebClient webClient;
    private final CookieUtils cookieUtil;
    private final ObjectMapper mapper;

    @Value("${keycloak.url}") private String keyCloakURI;
    @Value("${keycloak.userclientid}") private String userClientId;
    @Value("${keycloak.usersecret}") private String userClientSecret;
    @Value("${keycloak.adminclientid}") private String adminClientId;
    @Value("${keycloak.adminsecret}") private String adminClientSecret;
    @Value("${clients.user}") private String clientFrontendUrl;
    @Value("${clients.admin}") private String adminFrontendUrl;

    private static final SecureRandom secureRandom = new SecureRandom();

     
    public Mono<AuthServiceResult<LoginRedirectDto>> initiateLogin(ServerWebExchange exchange, String clientParam) {
        ClientInfoDto ci = resolveClient(exchange, clientParam);

        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        String state = generateState();

        String redirectUri = "https://localhost:8903/api/v1/bff/callback";

        UriComponentsBuilder loginUrl = UriComponentsBuilder.fromHttpUrl(keyCloakURI + "/auth")
                .queryParam("response_type", "code")
                .queryParam("client_id", ci.getClientId())
                .queryParam("scope", "openid profile email")
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                ;

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(loginUrl.toUriString()));
        // set cookies with cookieUtil
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.createCookie("OAUTH_STATE", state, 300).toString());
        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.createCookie("OAUTH_PKCE", codeVerifier, 300).toString());

        LoginRedirectDto dto = new LoginRedirectDto(loginUrl.toUriString(), ci.getClientId());
        return Mono.just(new AuthServiceResult<>(headers, dto, HttpStatus.FOUND));
    }
    public Mono<AuthServiceResult<Void>> handleCallback(String code, String state, ServerWebExchange exchange) {

        String stateCookie=exchange.getRequest().getCookies().getFirst("OAUTH_STATE").getValue();

        if (stateCookie == null || !state.equals(stateCookie)) {
            return Mono.just(new AuthServiceResult<>(null, null, HttpStatus.UNAUTHORIZED));
        }

        ClientInfoDto ci = resolveClient(exchange, null);
        String codeVerifier = exchange.getRequest().getCookies().getFirst("OAUTH_PKCE").getValue();

        String tokenEndpoint = keyCloakURI + "/token";
        MultiValueMap<String,String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("client_id", ci.getClientId());
        form.add("client_secret", ci.getClientSecret());
        form.add("redirect_uri", "https://localhost:8903/api/v1/bff/callback");
        if (codeVerifier != null) form.add("code_verifier", codeVerifier);

        return webClient.post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .exchangeToMono(resp -> resp.toEntity(String.class))
                .flatMap(respEntity -> {
                    if (!respEntity.getStatusCode().is2xxSuccessful()) {
                        return Mono.just(new AuthServiceResult<>(null, null, HttpStatus.BAD_REQUEST));
                    }
                    try {
                        TokenDto tokenDto = mapper.readValue(respEntity.getBody(), TokenDto.class);
                        HttpHeaders headers = createCookies(tokenDto);
                        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteCookie("OAUTH_STATE").toString());
                        headers.add(HttpHeaders.SET_COOKIE, cookieUtil.deleteCookie("OAUTH_PKCE").toString());
                        headers.setLocation(URI.create(ci.getRedirectURL()));
                        return Mono.just(new AuthServiceResult<>(headers, null, HttpStatus.FOUND));
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                });
    }
    public Mono<AuthServiceResult<TokenDto>> refreshAccessToken(String oldRefreshToken, ServerWebExchange exchange) {
        if (oldRefreshToken == null) {
            return Mono.just(new AuthServiceResult<>(null, null, HttpStatus.UNAUTHORIZED));
        }

        ClientInfoDto ci = resolveClient(exchange, null);
        String tokenEndpoint = keyCloakURI + "/token";

        MultiValueMap<String,String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "refresh_token");
        map.add("client_id", ci.getClientId());
        map.add("client_secret", ci.getClientSecret());
        map.add("refresh_token", oldRefreshToken);

        return webClient.post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(map))
                .exchangeToMono(resp -> resp.toEntity(String.class))
                .flatMap(entity -> {
                    if (!entity.getStatusCode().is2xxSuccessful()) {
                        return Mono.just(new AuthServiceResult<>(null, null, HttpStatus.BAD_REQUEST));
                    }
                    try {
                        TokenDto tokenDto = mapper.readValue(entity.getBody(), TokenDto.class);
                        HttpHeaders headers = createCookies(tokenDto);
                        return Mono.just(new AuthServiceResult<>(headers, tokenDto, HttpStatus.OK));
                    } catch (Exception ex) {
                        return Mono.error(ex);
                    }
                });
    }
    public Mono<AuthServiceResult<Void>> logout(ServerWebExchange exchange, String idToken) {
        ClientInfoDto ci = resolveClient(exchange, null);
        String url = UriComponentsBuilder.fromHttpUrl(keyCloakURI + "/logout")
                .queryParam("post_logout_redirect_uri", ci.getRedirectURL())
                .queryParam("id_token_hint", idToken != null ? idToken : "")
                .queryParam("client_id", ci.getClientId())
                .build().toUriString();

        return webClient.get()
                .uri(url)
                .retrieve()
                .toBodilessEntity()
                .map(resp -> {
                    HttpHeaders headers = cookieUtil.clearCookiesHeaders();
                    headers.setLocation(URI.create(ci.getRedirectURL()));
                    return new AuthServiceResult<>(headers, null, HttpStatus.FOUND);
                });
    }

    //HELPER METHODS
    private ClientInfoDto resolveClient(ServerWebExchange exchange, String clientParam) {

        String host = exchange.getRequest().getHeaders().getHost() != null ? exchange.getRequest().getHeaders().getHost().getHostName() : "";

        if ("admin".equalsIgnoreCase(clientParam) || host.contains("admin")) {
            return new ClientInfoDto(adminClientId, adminClientSecret, adminFrontendUrl);
        } else {
            return new ClientInfoDto(userClientId, userClientSecret, clientFrontendUrl);
        }
    }
    private HttpHeaders createCookies(TokenDto tokenDto) {
        HttpHeaders headers = new HttpHeaders();

        if (tokenDto.accessToken != null) {
            headers.add(HttpHeaders.SET_COOKIE,
                    cookieUtil.createCookie("AT", tokenDto.accessToken, tokenDto.expiresIn != null ? tokenDto.expiresIn : 20).toString());
        }
        if (tokenDto.refreshToken != null) {
            headers.add(HttpHeaders.SET_COOKIE,
                    cookieUtil.createCookie("RT", tokenDto.refreshToken, tokenDto.refreshExpiresIn != null ? tokenDto.refreshExpiresIn : 3600).toString());
        }
        if (tokenDto.idToken != null) {
            headers.add(HttpHeaders.SET_COOKIE,
                    cookieUtil.createCookie("IT", tokenDto.idToken, tokenDto.expiresIn != null ? tokenDto.expiresIn : 60).toString());
        }
        return headers;
    }
    private String generateCodeVerifier() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    private String generateCodeChallenge(String verifier) {
        try {
            byte[] sha = java.security.MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(sha);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private String generateState() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
