package com.virtualcard.transactionservice.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * @author lex_looter
 *
 *         11 giu 2025
 *
 */
@Configuration
public class TransactionServiceConfiguration {

	@Bean
	OpenAPI customOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("My API")
				.version("1.0")
				.description("Simple API for demonstration"));
	}

}
