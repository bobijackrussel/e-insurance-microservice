package com.einsurance.gateway.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/turnstile")
@RequiredArgsConstructor
public class TurnstileController {

    private final WebClient.Builder webClientBuilder;

    @Value("${turnstile.secret}")
    private String turnstileSecret;

    @Value("${clients.user:http://localhost:8904}")
    private String userFrontendOrigin;

    @Value("${turnstile.cookie-name:human}")
    private String humanCookieName;

    @Value("${turnstile.cookie-max-age-hours:24}")
    private long cookieTtlHours;

    @PostMapping("/verify")
    public Mono<ResponseEntity<String>> verify(@RequestBody Map<String, String> body,
                                               ServerHttpRequest request) {
        String token = body.get("token");
        if (token == null || token.isBlank()) {
            return Mono.just(ResponseEntity.badRequest().body("missing token"));
        }

        String remoteIp = request.getHeaders().getFirst("X-Forwarded-For");
        if (remoteIp == null && request.getRemoteAddress() != null) {
            remoteIp = request.getRemoteAddress().getAddress().getHostAddress();
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", turnstileSecret);
        form.add("response", token);
        if (remoteIp != null) {
            form.add("remoteip", remoteIp);
        }

        return webClientBuilder.baseUrl("https://challenges.cloudflare.com")
                .build()
                .post()
                .uri("/turnstile/v0/siteverify")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> {
                    boolean success = json.path("success").asBoolean(false);
                    if (success) {
                        ResponseCookie cookie = ResponseCookie.from(humanCookieName, "ok")
                                .httpOnly(true)
                                .secure(false)
                                .path("/")
                                .maxAge(Duration.ofHours(cookieTtlHours))
                                .sameSite("Lax")
                                .build();

                        return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, userFrontendOrigin)
                                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
                                .body("ok");
                    }
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("turnstile failed: " + json.path("error-codes"));
                })
                .onErrorResume(ex -> Mono.just(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("validation error")));
    }
}
