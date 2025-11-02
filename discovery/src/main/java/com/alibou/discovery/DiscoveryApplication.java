package com.alibou.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.event.EventListener;

import java.io.IOException;

@EnableEurekaServer
@SpringBootApplication
public class DiscoveryApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscoveryApplication.class, args);
	}

	/*@EventListener
	public void readKeys(ApplicationStartedEvent event) throws IOException {
		String absolutePath = getClass().getClassLoader().getResource("discovery.jks").getPath();
		System.out.println("Absolute Path: " + absolutePath);

		String keystore = System.getProperty("javax.net.ssl.keyStore");
		String keystorePassword = System.getProperty("javax.net.ssl.keyStorePassword");
		String truststore = System.getProperty("javax.net.ssl.trustStore");
		String truststorePassword = System.getProperty("javax.net.ssl.trustStorePassword");

		// Ispis informacija
		System.out.println("Keystore: " + (keystore != null ? keystore : "Nije postavljeno"));
		System.out.println("Keystore Password: " + (keystorePassword != null ? "Postavljeno" : "Nije postavljeno"));
		System.out.println("Truststore: " + (truststore != null ? truststore : "Nije postavljeno"));
		System.out.println("Truststore Password: " + (truststorePassword != null ? "Postavljeno" : "Nije postavljeno"));
	}*/

}
