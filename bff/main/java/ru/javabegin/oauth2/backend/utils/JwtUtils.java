package ru.javabegin.oauth2.backend.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Date;

public class JwtUtils {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final long ttlSeconds;

    JwtUtils(String secret,long ttlSeconds){
        this.algorithm=Algorithm.HMAC256(secret);
        this.verifier= JWT.require(algorithm).withIssuer("recaptcha-gateway").build();
        this.ttlSeconds=ttlSeconds;
    }

    public String generateToken(){
        Instant now= Instant.now();
        return JWT.create()
                .withIssuer("recaptcha-gateway")
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(ttlSeconds)))
                .sign(algorithm);
    }

}
