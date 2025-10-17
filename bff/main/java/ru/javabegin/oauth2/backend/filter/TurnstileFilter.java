package ru.javabegin.oauth2.backend.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Component
public class TurnstileFilter implements GlobalFilter, Ordered {

    private static final List<String> WHITELIST = List.of("/recaptcha/verify", "/recaptcha.html", "/favicon.ico", "/static/", "/css/", "/js/");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (path.startsWith("/api/secure/") || path.startsWith("/api/")) {
            HttpCookie cookie = exchange.getRequest().getCookies().getFirst("human");
            if (cookie == null || !"ok".equals(cookie.getValue())) {
                exchange.getResponse().setStatusCode(HttpStatus.TEMPORARY_REDIRECT);
                exchange.getResponse().getHeaders().setLocation(URI.create("https://localhost:8904/turnstile.html"));
                return exchange.getResponse().setComplete();
            }
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}