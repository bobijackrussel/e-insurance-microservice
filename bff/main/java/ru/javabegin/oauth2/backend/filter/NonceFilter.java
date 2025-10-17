package ru.javabegin.oauth2.backend.filter;


import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class NonceFilter implements WebFilter {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String nonce = generateNonce();

        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add("Content-Security-Policy",
                "script-src 'self' 'nonce-" + nonce + "'");

        exchange.getAttributes().put("cspNonce", nonce);

        return chain.filter(exchange);
    }

    private String generateNonce() {
        byte[] nonceBytes = new byte[16];
        secureRandom.nextBytes(nonceBytes);
        return base64Encoder.encodeToString(nonceBytes);
    }
}
