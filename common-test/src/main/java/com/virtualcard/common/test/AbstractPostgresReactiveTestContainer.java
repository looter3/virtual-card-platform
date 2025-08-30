package com.virtualcard.common.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.hibernate.reactive.mutiny.Mutiny;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import lombok.extern.log4j.Log4j2;

import io.smallrye.mutiny.Uni;

/**
 * @author Lorenzo Leccese
 *
 *         30 ago 2025
 *
 */

@Log4j2
public abstract class AbstractPostgresReactiveTestContainer {

	private static final String DB_NAME = "card_system";
	private static final String USER_PASS = "test";
	private static final String POSTGRES_IMG_VERSION = "postgres:15.3";

	private static final String SCHEMA_SQL = "schema.sql";
	private static final String SPRING_DATASOURCE_PASSWORD = "spring.datasource.password";
	private static final String SPRING_DATASOURCE_USERNAME = "spring.datasource.username";
	private static final String SPRING_DATASOURCE_URL = "spring.datasource.url";

	@SuppressWarnings("resource")
	static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMG_VERSION)
		.withDatabaseName(DB_NAME)
		.withUsername(USER_PASS)
		.withPassword(USER_PASS);

	static {
		postgres.start();
	}

	protected static final String POSTGRES_DATASOURCE = "jdbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/" + postgres.getDatabaseName();

	@BeforeAll
	protected void setup() {
		/*-
				try {
					this.setupSchema();
				} catch (final IOException e) {
					log.error("Failed test setup", e);
				}
			*/
	}

	/**
	 * Execute schema.sql statements using Hibernate Reactive.
	 * You need to provide a Mutiny.SessionFactory instance from your test configuration.
	 */
	protected void setupSchema() throws IOException {
		final String schema = new String(
				Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(SCHEMA_SQL))
					.readAllBytes(),
				StandardCharsets.UTF_8);

		final String[] statements = schema.split(";");

		// Must execute statements sequentially
		for (final String statement : statements) {
			if (!statement.trim().isEmpty()) {
				executeSql(statement.trim())
					.await().indefinitely(); // block only for test setup
			}
		}
	}

	/**
	 * Override this method in your test to provide the Hibernate Reactive session factory.
	 */
	protected abstract Mutiny.SessionFactory getSessionFactory();

	private Uni<Void> executeSql(final String sql) {

		return getSessionFactory()
			.withSession(session -> session.createNativeQuery(sql)
				.executeUpdate()
				.replaceWithVoid());
	}

	@DynamicPropertySource
	static void overrideProperties(final DynamicPropertyRegistry registry) {
		registry.add(SPRING_DATASOURCE_URL, () -> POSTGRES_DATASOURCE);
		registry.add(SPRING_DATASOURCE_USERNAME, postgres::getUsername);
		registry.add(SPRING_DATASOURCE_PASSWORD, postgres::getPassword);
	}
}
