package com.virtualcard.common.test;

import static com.virtualcard.common.lang.LangConstants.COLON;
import static com.virtualcard.common.lang.LangConstants.SEMICOLON;
import static com.virtualcard.common.lang.LangConstants.SLASH;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

import lombok.extern.log4j.Log4j2;

/**
 * @author lex_looter
 *
 *         9 giu 2025
 *
 */
@Log4j2
public abstract class AbstractMySQLTestContainerTest {

	// These three could be properties
	private static final String DB_NAME = "card_system";
	private static final String USER_PASS = "test";
	private static final String MYSQL_IMG_VERSION = "mysql:8.0";

	private static final String JDBC_MYSQL = "jdbc:mysql://";
	private static final String SCHEMA_SQL = "schema.sql";
	private static final String SPRING_DATASOURCE_PASSWORD = "spring.datasource.password";
	private static final String SPRING_DATASOURCE_USERNAME = "spring.datasource.username";
	private static final String SPRING_DATASOURCE_URL = "spring.datasource.url";

	@Autowired
	protected DSLContext dsl;

	@SuppressWarnings("resource")
	static final MySQLContainer<?> mysql = new MySQLContainer<>(MYSQL_IMG_VERSION)
		.withDatabaseName(DB_NAME)
		.withUsername(USER_PASS)
		.withPassword(USER_PASS);

	static {
		mysql.start(); // start container once
	}

	private static final String MYSQL_DATASOURCE = JDBC_MYSQL + mysql.getHost() + COLON + mysql.getFirstMappedPort() + SLASH + mysql.getDatabaseName();

	@BeforeAll
	protected void setup() {
		try {
			this.setupSchema();
		} catch (final IOException e) {
			log.error("Failed test setup");
		}
	}

	private void setupSchema() throws IOException {

		final String schema = new String(
				Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(SCHEMA_SQL))
					.readAllBytes(),
				StandardCharsets.UTF_8);

		// Split statements by semicolons and execute each one separately
		final String[] statements = schema.split(SEMICOLON);
		for (final String statement : statements) {
			if (!statement.trim().isEmpty()) {
				dsl.execute(statement.trim());
			}
		}
	}

	@DynamicPropertySource
	static void overrideProperties(final DynamicPropertyRegistry registry) {

		// JDBC properties for jOOQ
		registry.add(SPRING_DATASOURCE_URL, () -> MYSQL_DATASOURCE);
		registry.add(SPRING_DATASOURCE_USERNAME, mysql::getUsername);
		registry.add(SPRING_DATASOURCE_PASSWORD, mysql::getPassword);
	}

}
