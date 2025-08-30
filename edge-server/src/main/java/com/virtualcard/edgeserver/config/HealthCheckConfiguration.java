package com.virtualcard.edgeserver.config;

import static java.util.logging.Level.FINE;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import reactor.core.publisher.Mono;

/**
 * @author Lorenzo Leccese
 *
 *         5 lug 2025
 *
 */
@Configuration
@RequiredArgsConstructor
@Log4j2
public class HealthCheckConfiguration {

	private final WebClient webClient;

	@Bean
	ReactiveHealthContributor healthcheckMicroservices() {

		final Map<String, ReactiveHealthIndicator> registry = new LinkedHashMap<>();

		registry.put("card", () -> getHealth("http://card-service"));
		registry.put("transaction", () -> getHealth("http://transaction-service"));
		registry.put("card-aggregate", () -> getHealth("http://card-aggregate-service"));

		return CompositeReactiveHealthContributor.fromMap(registry);
	}

	private Mono<Health> getHealth(final String baseUrl) {
		final String url = baseUrl + "/actuator/health";
		log.debug("Setting up a call to the Health API on URL: {}", url);
		return webClient.get().uri(url).retrieve().bodyToMono(String.class)
			.map(s -> new Health.Builder().up().build())
			.onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
			.log(log.getName(), FINE);
	}

}
