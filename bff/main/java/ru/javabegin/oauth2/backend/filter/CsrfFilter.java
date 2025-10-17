package ru.javabegin.oauth2.backend.filter;

import org.springframework.http.ResponseCookie;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;


@Component
public class CsrfFilter  implements WebFilter
{
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain){
        Mono<org.springframework.security.web.server.csrf.CsrfToken> token = exchange.getAttributeOrDefault(org.springframework.security.web.server.csrf.CsrfToken.class.getName(), Mono.empty());
        return token.then(chain.filter(exchange));

    }
}
