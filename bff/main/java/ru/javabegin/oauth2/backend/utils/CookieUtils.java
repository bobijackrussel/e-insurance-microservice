package ru.javabegin.oauth2.backend.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
public class CookieUtils {

    @Value("${cookie.domain:}")
    private String cookieDomain;

    public ResponseCookie createCookie(String name, String value, int durationInSeconds) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, value)
                .maxAge(Duration.ofSeconds(durationInSeconds))
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/");
        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            b.domain(cookieDomain);
        }
        return b.build();
    }
    public ResponseCookie deleteCookie(String name){
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, "")
                .maxAge(Duration.ZERO)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/");
        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            b.domain(cookieDomain);
        }
        return b.build();
    }
    public HttpHeaders clearCookiesHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, deleteCookie("AT").toString());
        headers.add(HttpHeaders.SET_COOKIE, deleteCookie("RT").toString());
        headers.add(HttpHeaders.SET_COOKIE, deleteCookie("IT").toString());
        headers.add(HttpHeaders.SET_COOKIE, deleteCookie("XSRF-TOKEN").toString());
        return headers;
    }
    public void clearCookies(ServerHttpResponse response){
        ResponseCookie at = ResponseCookie.from("AT", "").path("/").maxAge(0).build();
        ResponseCookie rt = ResponseCookie.from("RT", "").path("/").maxAge(0).build();
        response.getHeaders().add(HttpHeaders.SET_COOKIE, at.toString());
        response.getHeaders().add(HttpHeaders.SET_COOKIE, rt.toString());

    }
}
