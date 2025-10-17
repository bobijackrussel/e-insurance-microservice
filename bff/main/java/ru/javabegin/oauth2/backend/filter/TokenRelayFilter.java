package ru.javabegin.oauth2.backend.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.javabegin.oauth2.backend.dto.TokenDto;
import ru.javabegin.oauth2.backend.service.AuthService;
import ru.javabegin.oauth2.backend.utils.CookieUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
public class TokenRelayFilter implements GlobalFilter, Ordered {

    @Autowired private final AuthService authService;
    @Autowired private final CookieUtils cookieUtils;

    private static final List<String> ALLOWED_PATHS = List.of("/login");
    private static final ConcurrentMap<String, Mono<String>> refreshInProgress = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        for (String allowed : ALLOWED_PATHS) {
            if (path.endsWith(allowed)) return chain.filter(exchange);
        }

        var cookies = exchange.getRequest().getCookies();
        var atCookie = cookies.getFirst("AT");
        var rtCookie = cookies.getFirst("RT");

        if (atCookie != null) {
            return continueWithAccessToken(exchange, chain, atCookie.getValue());
        }
        else if (rtCookie == null) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        else
        {
            String rtValue = rtCookie.getValue();
            Mono<String> refresher = refreshInProgress.computeIfAbsent(rtValue, key ->
                    doServerRefresh(exchange, rtValue)
                            .doFinally(sig -> refreshInProgress.remove(key))
                            .cache()
            );

            return refresher.flatMap(accessToken -> {
                System.out.println("CASE:1");
                if (accessToken == null || accessToken.isBlank()) {
                    cookieUtils.clearCookies(exchange.getResponse());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
                return continueWithAccessToken(exchange, chain, accessToken);
            }).switchIfEmpty(Mono.defer(() -> {
                System.out.println("CASE:2");
                /*clearCookies(exchange.getResponse());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);*/
                return exchange.getResponse().setComplete();
            }));
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private Mono<Void> continueWithAccessToken(ServerWebExchange exchange, GatewayFilterChain chain, String accessToken) {
        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .headers(h -> {
                    h.remove(HttpHeaders.COOKIE);
                    h.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
                }).build();
        return chain.filter(exchange.mutate().request(mutated).build());
    }
    private Mono<String> doServerRefresh(ServerWebExchange exchange, String rtValue) {
        return authService.refreshAccessToken(rtValue, exchange)
                .flatMap(authResult -> {
                    if (authResult == null) return Mono.empty();

                    HttpStatus status = authResult.getStatus(); // assume getter
                    if (status == null || !status.is2xxSuccessful()) {
                        return Mono.empty();
                    }

                    HttpHeaders headers = authResult.getHeaders();
                    if (headers != null && !headers.isEmpty()) {
                        exchange.getResponse().getHeaders().putAll(headers);
                    }

                    TokenDto tokenDto = authResult.getBody();
                    if (tokenDto == null || tokenDto.getAccessToken() == null || tokenDto.getAccessToken().isBlank()) {
                        return Mono.empty();
                    }

                    return Mono.just(tokenDto.getAccessToken());
                });
    }

}
