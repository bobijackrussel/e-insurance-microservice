package com.einsurance.gateway.controller;

import com.einsurance.gateway.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/bff")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Mono<ResponseEntity<Void>> login(ServerWebExchange exchange,
                                            @RequestParam(value = "client", required = false) String clientParam) {
        return authService.initiateLogin(exchange, clientParam)
                .map(result -> ResponseEntity.status(result.getStatus()).headers(result.getHeaders()).build());
    }

    @GetMapping("/callback")
    public Mono<Void> callback(@RequestParam String code,
                               @RequestParam String state,
                               ServerWebExchange exchange) {
        return authService.handleCallback(code, state, exchange)
                .flatMap(result -> {
                    var response = exchange.getResponse();
                    response.setStatusCode(result.getStatus());
                    response.getHeaders().putAll(result.getHeaders());
                    return response.setComplete();
                });
    }

    @PostMapping("/newaccesstoken")
    public Mono<ResponseEntity<Void>> newAccessToken(
            @CookieValue(value = "RT", required = false) String refreshToken,
            ServerWebExchange exchange) {
        return authService.refreshAccessToken(refreshToken, exchange)
                .map(result -> ResponseEntity.status(result.getStatus()).headers(result.getHeaders()).build());
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(
            @CookieValue(value = "IT", required = false) String idToken,
            ServerWebExchange exchange) {
        return authService.logout(exchange, idToken)
                .map(result -> ResponseEntity.status(result.getStatus()).headers(result.getHeaders()).build());
    }
}
