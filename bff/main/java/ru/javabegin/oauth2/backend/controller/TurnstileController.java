package ru.javabegin.oauth2.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/turnstile")
public class TurnstileController {

    private final WebClient webClient;
    @Value("${turnstile.secret}")
    private String secret;

    public TurnstileController(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://challenges.cloudflare.com").build();
    }

    @PostMapping("/verify")
    public Mono<ResponseEntity<String>> verify(@RequestBody Map<String, String> body,
                                               ServerHttpRequest request) {
        String token = body.get("token");
        if (token == null || token.isBlank()) {
            return Mono.just(ResponseEntity.badRequest().body("missing token"));
        }

        String remoteip = request.getHeaders().getFirst("X-Forwarded-For");
        if (remoteip == null) remoteip = request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : null;

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", secret);
        form.add("response", token);
        if (remoteip != null) form.add("remoteip", remoteip);

        return webClient.post()
                .uri("/turnstile/v0/siteverify")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> {
                    boolean success = json.path("success").asBoolean(false);
                    if (success) {
                        ResponseCookie cookie = ResponseCookie.from("human","ok")
                                .httpOnly(true)
                                .secure(true)
                                .path("/")
                                .maxAge(Duration.ofDays(1))
                                .sameSite("Lax")
                                .build();

                        return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://localhost:8904")
                                .header(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
                                .body("ok");
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("turnstile failed: " + json.path("error-codes").toString());
                    }
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("validation error")));
    }
}
