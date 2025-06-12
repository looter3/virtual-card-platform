package com.virtualcard.card_aggregate.test;

import static com.virtualcard.common.lang.LangConstants.SLASH;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import com.jooq.generated.enums.TransactionType;
import com.virtualcard.card_aggregate.configuration.IntegrationProperties;
import com.virtualcard.card_aggregate.service.CardAggregateIntegrationService;
import com.virtualcard.common.error.RateLimitExceededException;

import lombok.extern.log4j.Log4j2;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author lex_looter
 *
 *         10 giu 2025
 *
 */
@Log4j2
public class CardAggregateServiceTest {

	private static final String APPLICATION_JSON = "application/json";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String BALANCE = "{\"balance\": %s}";

	// "MockWebServer.enqueue()" enqueue responses and then retrieve them in a FIFO order like a regular Queue
	private MockWebServer mockWebServer;
	private CardAggregateIntegrationService service;
	private IntegrationProperties integrationProperties;

	@BeforeEach
	void setUp() throws Exception {
		mockWebServer = new MockWebServer();
		mockWebServer.start();

		final WebClient webClient = WebClient.builder()
			.baseUrl(mockWebServer.url(SLASH).toString())
			.build();

		final String baseUrl = mockWebServer.url(SLASH).toString();

		integrationProperties = new IntegrationProperties();
		integrationProperties.setCardServiceBaseUrl(baseUrl);
		integrationProperties.setTransactionServiceBaseUrl(baseUrl);

		service = new CardAggregateIntegrationService(webClient, integrationProperties);
	}

	@AfterEach
	void tearDown() throws Exception {
		mockWebServer.shutdown();
	}

	private Mono<BigDecimal> balanceOperation_helperFunction(final String cardId, final String balanceAsString, final BigDecimal amount, final TransactionType type,
			final int updateBalanceReturnCode, final int createTransactionReturnCode) {

		// GET /cards/card123
		mockWebServer.enqueue(new MockResponse()
			.setBody(BALANCE.formatted(balanceAsString))
			.addHeader(CONTENT_TYPE, APPLICATION_JSON));

		// PUT /cards/card123/balance
		mockWebServer.enqueue(new MockResponse().setResponseCode(updateBalanceReturnCode));

		// POST /transactions
		mockWebServer.enqueue(new MockResponse().setResponseCode(createTransactionReturnCode));

		return switch (type) {
			case SPEND -> service.spend(cardId, amount);
			case TOPUP -> service.topup(cardId, amount);
			default -> throw new IllegalArgumentException("Unexpected value: " + type);
		};
	}

	@Test
	void testTopup_shouldSucceed() {

		final Mono<BigDecimal> result = balanceOperation_helperFunction("card123", "100", new BigDecimal("50"), TransactionType.TOPUP, 200, 200);

		StepVerifier.create(result)
			.expectNext(new BigDecimal("150"))
			.verifyComplete();
	}

	@Test
	void testTopup_shouldFail() {

		final Mono<BigDecimal> result = balanceOperation_helperFunction("card123", "100", new BigDecimal("50"), TransactionType.TOPUP, 500, 200);

		StepVerifier.create(result)
			.expectError()
			.verify();
	}

	@Test
	void testSpend_shouldSucceed() {

		final Mono<BigDecimal> result = balanceOperation_helperFunction("card456", "200", new BigDecimal("75"), TransactionType.SPEND, 200, 200);

		StepVerifier.create(result)
			.expectNext(new BigDecimal("125"))
			.verifyComplete();
	}

	@Test
	void testSpend_shouldFail() {

		final Mono<BigDecimal> result = balanceOperation_helperFunction("card456", "60", new BigDecimal("75"), TransactionType.SPEND, 500, 200);

		StepVerifier.create(result)
			.expectError()
			.verify();
	}

	@Test
	void testSpend_rateLimitExceeded() {

		final String cardId = "rateLimitCard";
		final BigDecimal amount = new BigDecimal("10");

		// Enqueue 6 sets of responses for GET /cards, PUT balance, POST transaction
		for (int i = 0; i < 6; i++) {
			// GET card balance (assume 100)
			mockWebServer.enqueue(new MockResponse()
				.setBody(BALANCE.formatted("100"))
				.addHeader(CONTENT_TYPE, APPLICATION_JSON));

			// PUT update balance (200 OK)
			mockWebServer.enqueue(new MockResponse().setResponseCode(200));

			// POST create transaction (200 OK)
			mockWebServer.enqueue(new MockResponse().setResponseCode(200));
		}

		// The first 5 spends should succeed, 6th should fail with RateLimitExceededException
		StepVerifier.create(service.spend(cardId, amount)).expectNextMatches(balance -> balance.compareTo(new BigDecimal("90")) == 0).verifyComplete();
		StepVerifier.create(service.spend(cardId, amount)).expectNextMatches(balance -> balance.compareTo(new BigDecimal("90")) == 0).verifyComplete();
		StepVerifier.create(service.spend(cardId, amount)).expectNextMatches(balance -> balance.compareTo(new BigDecimal("90")) == 0).verifyComplete();
		StepVerifier.create(service.spend(cardId, amount)).expectNextMatches(balance -> balance.compareTo(new BigDecimal("90")) == 0).verifyComplete();
		StepVerifier.create(service.spend(cardId, amount)).expectNextMatches(balance -> balance.compareTo(new BigDecimal("90")) == 0).verifyComplete();

		StepVerifier.create(service.spend(cardId, amount))
			.expectErrorMatches(throwable -> throwable instanceof RateLimitExceededException
					&& throwable.getMessage().contains("Max 5 spends per minute exceeded"))
			.verify();
	}

}
