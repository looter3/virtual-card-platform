package com.virtualcard.cardservice.configuration;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * @author Lorenzo Leccese
 *
 *         11 giu 2025
 *
 */
@Configuration
public class CardServiceConfiguration {

	/**
	 * Configures and provides a DSLContext bean for interacting with a MySQL database using jOOQ.
	 *
	 * @param dataSource the DataSource to be used for database connections
	 * @return a configured DSLContext instance for executing SQL queries
	 */
	@Bean
	DSLContext dslContext(final DataSource dataSource) {
		return DSL.using(dataSource, SQLDialect.MYSQL,
				new Settings()
					.withExecuteWithOptimisticLocking(true)
					.withUpdateRecordVersion(true));
	}

	@Bean
	OpenAPI customOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("My API")
				.version("1.0")
				.description("Simple API for demonstration"));
	}

}
