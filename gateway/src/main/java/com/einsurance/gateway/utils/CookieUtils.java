package com.einsurance.gateway.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Component
public class CookieUtils {

    @Value("${cookie.domain:}")
    private String cookieDomain;

    @Value("${cookie.secure:false}")
    private boolean secureCookies;

    @Value("${cookie.same-site:Lax}")
    private String sameSitePolicy;

    public ResponseCookie createCookie(String name, String value, int durationInSeconds) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .maxAge(Duration.ofSeconds(durationInSeconds))
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite(sameSitePolicy)
                .path("/");
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }
        return builder.build();
    }

    public ResponseCookie deleteCookie(String name) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, "")
                .maxAge(Duration.ZERO)
                .httpOnly(true)
                .secure(secureCookies)
                .sameSite(sameSitePolicy)
                .path("/");
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }
        return builder.build();
    }

    public HttpHeaders clearCookiesHeaders(String... additionalCookies) {
        HttpHeaders headers = new HttpHeaders();
        List<String> cookiesToClear = Arrays.asList("AT", "RT", "IT", "XSRF-TOKEN");
        cookiesToClear.forEach(cookie -> headers.add(HttpHeaders.SET_COOKIE, deleteCookie(cookie).toString()));
        if (additionalCookies != null) {
            Arrays.stream(additionalCookies)
                    .filter(name -> name != null && !name.isBlank())
                    .forEach(name -> headers.add(HttpHeaders.SET_COOKIE, deleteCookie(name).toString()));
        }
        return headers;
    }

    public void clearCookies(ServerHttpResponse response) {
        response.getHeaders().add(HttpHeaders.SET_COOKIE, deleteCookie("AT").toString());
        response.getHeaders().add(HttpHeaders.SET_COOKIE, deleteCookie("RT").toString());
        response.getHeaders().add(HttpHeaders.SET_COOKIE, deleteCookie("IT").toString());
    }
}
