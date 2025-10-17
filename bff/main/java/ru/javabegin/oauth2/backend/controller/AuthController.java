package ru.javabegin.oauth2.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.javabegin.oauth2.backend.service.AuthService;


@RestController
@RequestMapping("/api/v1/bff")
@RequiredArgsConstructor
public class AuthController {

    @Autowired private final AuthService authService;

    // ---- LOGIN ----
    @PostMapping("/login")
    public Mono<ResponseEntity<Void>> login(ServerWebExchange exchange,
                                                        @RequestParam(value = "client", required = false) String clientParam) {
        return authService.initiateLogin(exchange, clientParam)
                .map(sr -> ResponseEntity.status(sr.getStatus()).headers(sr.getHeaders()).build());
    }

    // ---- CALLBACK ----
    @GetMapping("/callback")
    public Mono<Void> callback(@RequestParam String code,
                               @RequestParam String state,
                               ServerWebExchange exchange) {
        // service returns headers & intent; controller writes them directly into ServerHttpResponse
        return authService.handleCallback(code, state, exchange)
                .flatMap(sr -> {
                    var response = exchange.getResponse();
                    response.setStatusCode(sr.getStatus());
                    response.getHeaders().putAll(sr.getHeaders());
                    return response.setComplete();
                });
    }

    // ---- REFRESH ----
    @PostMapping("/newaccesstoken")
    public Mono<Void> newAccessToken(@CookieValue(value="RT", required=false) String oldRefreshToken,
                                                         ServerWebExchange exchange) {
        return authService.refreshAccessToken(oldRefreshToken, exchange)
                .flatMap(sr -> {
                    if (sr.getHeaders() != null && !sr.getHeaders().isEmpty()) {
                        exchange.getResponse().getHeaders().putAll(sr.getHeaders());
                    }
                    return Mono.empty();
                });
    }

    // ---- LOGOUT ----
    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(@CookieValue(value="IT", required=false) String idToken,
                                               ServerWebExchange exchange) {
        return authService.logout(exchange, idToken)
                .map(sr -> ResponseEntity.status(sr.getStatus()).headers(sr.getHeaders()).build());
    }


}
