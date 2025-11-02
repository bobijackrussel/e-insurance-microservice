package com.alibou.discovery;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.config.client.ConfigServicePropertySourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;

//@Configuration
public class SSLConfigServiceBootstrapConfiguration {

    @Autowired
    ConfigClientProperties properties;

    @Bean
    public ConfigServicePropertySourceLocator configServicePropertySourceLocator() throws Exception {
        final char[] password = "123456".toCharArray();
        final File keyStoreFile = new ClassPathResource("discovery.jks").getFile();
        final File trustStore=    new ClassPathResource("truststore.jks").getFile();
        SSLContext sslContext = SSLContexts.custom()
                .loadKeyMaterial(keyStoreFile, password, password)
                .loadTrustMaterial(trustStore, password, null)
                .build();

        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
        HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create().setSSLSocketFactory(socketFactory).build();
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        ConfigServicePropertySourceLocator configServicePropertySourceLocator = new ConfigServicePropertySourceLocator(properties);
        configServicePropertySourceLocator.setRestTemplate(new RestTemplate(requestFactory));
        return configServicePropertySourceLocator;
    }
}
