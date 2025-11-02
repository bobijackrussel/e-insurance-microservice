package com.einsurance.gateway.filter;

import com.einsurance.gateway.dto.TokenDto;
import com.einsurance.gateway.service.AuthService;
import com.einsurance.gateway.service.AuthServiceResult;
import com.einsurance.gateway.utils.CookieUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
public class TokenRelayFilter implements GlobalFilter, Ordered {

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/bff/login",
            "/api/v1/bff/callback",
            "/api/v1/bff/logout",
            "/api/v1/bff/newaccesstoken",
            "/api/turnstile/verify"
    );

    private final AuthService authService;
    private final CookieUtils cookieUtils;

    private static final ConcurrentMap<String, Mono<String>> refreshInFlight = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        var cookies = exchange.getRequest().getCookies();
        var accessCookie = cookies.getFirst("AT");
        var refreshCookie = cookies.getFirst("RT");

        if (accessCookie != null && !accessCookie.getValue().isBlank()) {
            return continueWithAccessToken(exchange, chain, accessCookie.getValue());
        }

        if (refreshCookie == null || refreshCookie.getValue().isBlank()) {
            cookieUtils.clearCookies(exchange.getResponse());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String refreshToken = refreshCookie.getValue();
        Mono<String> refresher = refreshInFlight.computeIfAbsent(refreshToken, key ->
                authService.refreshAccessToken(refreshToken, exchange)
                        .flatMap(result -> handleRefreshResult(result, exchange))
                        .doFinally(signal -> refreshInFlight.remove(key))
                        .cache()
        );

        return refresher.flatMap(token -> {
            if (token == null || token.isBlank()) {
                cookieUtils.clearCookies(exchange.getResponse());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            return continueWithAccessToken(exchange, chain, token);
        }).switchIfEmpty(Mono.defer(() -> {
            cookieUtils.clearCookies(exchange.getResponse());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }));
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<String> handleRefreshResult(AuthServiceResult<TokenDto> result,
                                             ServerWebExchange exchange) {
        if (result == null || !result.getStatus().is2xxSuccessful() || result.getBody() == null) {
            return Mono.empty();
        }

        HttpHeaders headers = result.getHeaders();
        if (headers != null && !headers.isEmpty()) {
            exchange.getResponse().getHeaders().putAll(headers);
        }
        return Mono.justOrEmpty(result.getBody().getAccessToken());
    }

    private Mono<Void> continueWithAccessToken(ServerWebExchange exchange,
                                               GatewayFilterChain chain,
                                               String accessToken) {
        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(HttpHeaders.COOKIE);
                    headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
                })
                .build();
        return chain.filter(exchange.mutate().request(mutated).build());
    }
}
