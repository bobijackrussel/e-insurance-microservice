package ru.javabegin.oauth2.backend.security;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestHandler;
import org.springframework.security.web.server.csrf.XorServerCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.header.XXssProtectionServerHttpHeadersWriter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.cors.CorsConfiguration;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.time.Duration;
import java.util.Arrays;

import org.springframework.core.io.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class SpringSecurityConfig {

   @Value("${clients.admin}") private String adminClient;
   @Value("${clients.user}")  private String userClient;
   @Value("${resourceserver.url}") private String resourceURL;

   @Value("${trust.store}") private Resource trustStore;
   @Value("${trust.store-password}") private String trustStorePassword;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        CookieServerCsrfTokenRepository repo = CookieServerCsrfTokenRepository.withHttpOnlyFalse();
        repo.setCookieName("XSRF-TOKEN");

        repo.setCookieCustomizer(responseCookieBuilder ->
                responseCookieBuilder
                        .httpOnly(false)
                        .maxAge(Duration.ofMinutes(30))
                        .secure(true)
                        .path("/")
                        .sameSite("Lax"));

        XorServerCsrfTokenRequestAttributeHandler delegate = new XorServerCsrfTokenRequestAttributeHandler();
        ServerCsrfTokenRequestHandler requestHandler = delegate::handle;

        var csrfRequiredPaths = ServerWebExchangeMatchers.pathMatchers(
                HttpMethod.POST, "/admin/**", "/user/**","/api/user/**" );

        String cspPolicy = String.join(" ",
                "default-src 'self';",
                "script-src 'self';",
                "style-src 'self';",
                "img-src 'self' data: https:;",
                "object-src 'none';",
                "frame-ancestors 'none';",
                "base-uri 'self';",
                "form-action 'self';"
        );

        http
                .headers(headers -> headers
                        .xssProtection(xss -> xss.headerValue(XXssProtectionServerHttpHeadersWriter.HeaderValue.ENABLED)
                        .contentSecurityPolicy(spec -> spec.policyDirectives(cspPolicy))
                        /* HSTS - enforce HTTPS (only if you serve over HTTPS)
                        //.httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                        // prevent MIME type sniffing
                        // .defaultsDisabled() */))
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.csrfTokenRepository(repo)
                                           .requireCsrfProtectionMatcher(csrfRequiredPaths)
                                           .csrfTokenRequestHandler(requestHandler));
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowCredentials(true);
        configuration.setAllowedOrigins(Arrays.asList(userClient, adminClient, "https://localhost:9098"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));
        configuration.setExposedHeaders(Arrays.asList("Set-Cookie","Location"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) throws Exception {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream is = trustStore.getInputStream()) {
            ks.load(is, trustStorePassword.toCharArray());
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        SslContext sslContext = SslContextBuilder.forClient().trustManager(tmf).build();
        HttpClient httpClient = HttpClient.create().secure(spec -> spec.sslContext(sslContext));
        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        return builder.clientConnector(connector).build();
    }
}

