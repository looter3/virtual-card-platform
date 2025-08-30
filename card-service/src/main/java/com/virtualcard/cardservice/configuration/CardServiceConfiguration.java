package com.virtualcard.cardservice.configuration;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.virtualcard.common.configuration.SpringServiceConfiguration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * @author Lorenzo Leccese
 *
 *         11 giu 2025
 *
 */
@Configuration
public class CardServiceConfiguration extends SpringServiceConfiguration {

	/**
	 * Configures and provides a DSLContext bean for interacting with a MySQL database using jOOQ.
	 *
	 * @param dataSource the DataSource to be used for database connections
	 * @return a configured DSLContext instance for executing SQL queries
	 */
	/*-
	@Bean
	DSLContext dslContext(final DataSource dataSource) {
		return DSL.using(dataSource, SQLDialect.MYSQL,
				new Settings()
					.withExecuteWithOptimisticLocking(true)
					.withUpdateRecordVersion(true));
	}
	*/

	@Bean
	OpenAPI customOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("My API")
				.version("1.0")
				.description("Simple API for demonstration"));
	}

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
