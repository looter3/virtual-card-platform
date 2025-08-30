package com.virtualcard.user_service.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
public class UserServiceConfiguration extends SpringServiceConfiguration {

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

}
