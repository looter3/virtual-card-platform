package com.virtualcard.edgeserver.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Lorenzo Leccese
 *
 *         5 lug 2025
 *
 */
@Configuration
public class EdgeServerConfiguration {

	@Bean
	@LoadBalanced
	WebClient.Builder loadBalancedWebClientBuilder() {
		return WebClient.builder();
	}

	@Bean
	@LoadBalanced
	WebClient webClient(final WebClient.Builder builder) {
		return builder.build();
	}

}
