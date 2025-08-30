package com.virtualcard.card_aggregate.test;

import static com.virtualcard.common.lang.LangConstants.SLASH;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import com.virtualcard.card_aggregate.configuration.IntegrationProperties;
import com.virtualcard.card_aggregate.service.CardAggregateIntegrationService;
import com.virtualcard.common.enums.TransactionType;
import com.virtualcard.common.error.RateLimitExceededException;
import com.virtualcard.common.request.BalanceOperationRequest;

import lombok.extern.log4j.Log4j2;

import jakarta.annotation.PostConstruct;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * @author Lorenzo Leccese
 *
 *         10 giu 2025
 *
 */
@Log4j2
public class CardAggregateServiceTest {

	private static final String APPLICATION_JSON = "application/json";
	private static final String CONTENT_TYPE = "Content-Type";
//	private static final String BALANCE = "{\"balance\": %s}";
	private static final String CARD_JSON = """
			{
			  "id": %d,
			  "userId": %d,
			  "code": "%s",
			  "balance": %s,
			  "createdAt": "%s",
			  "status": "%s",
			  "version": %d,
			  "cvc": "%s",
			  "expirationDate": "%s"
			}
			""";

	// "MockWebServer.enqueue()" enqueue responses and then retrieve them in a FIFO order like a regular Queue
	private MockWebServer mockWebServer;
	private CardAggregateIntegrationService service;
	private IntegrationProperties integrationProperties;

	/**
	 * Sets up the test environment before each test execution.
	 *
	 * Initializes and starts a MockWebServer instance to simulate HTTP server behavior.
	 * It configures a `WebClient` instance with the base URL derived from the MockWebServer.
	 *
	 * Populates the `IntegrationProperties` object with the appropriate base URLs
	 * for card and transaction services using the MockWebServer's URL.
	 *
	 * Instantiates a `CardAggregateIntegrationService` object to be used in test cases.
	 *
	 * @throws Exception if an error occurs during the setup process.
	 */
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
		invokePostConstruct(service);

	}

	public static void invokePostConstruct(final Object obj) {
		for (final Method method : obj.getClass().getDeclaredMethods()) {
			if (method.isAnnotationPresent(PostConstruct.class)) {
				method.setAccessible(true);
				try {
					method.invoke(obj);
				} catch (final Exception e) {
					throw new RuntimeException("Failed to invoke @PostConstruct method: " + method, e);
				}
			}
		}
	}

	@AfterEach
	void tearDown() throws Exception {
		mockWebServer.shutdown();
	}

	private void balanceOperation_helperFunction(
			final BigDecimal senderCurrentBalance,
			final int updateBalanceReturnCode,
			final int createTransactionReturnCode) {

		// Sender GET: /cards/covered/{sender}?amount=50
		mockWebServer.enqueue(new MockResponse()
			.setBody(buildCardJson(1L, 10L, "123", senderCurrentBalance, "123"))
			.addHeader("Content-Type", "application/json"));

		// Recipient GET: /cards/{recipient}
		mockWebServer.enqueue(new MockResponse()
			.setBody(buildCardJson(2L, 20L, "456", new BigDecimal("200.00"), "456"))
			.addHeader("Content-Type", "application/json"));

		// PUT /cards/{senderId}/updateBalance
		mockWebServer.enqueue(new MockResponse().setResponseCode(updateBalanceReturnCode));

		// PUT /cards/{recipientId}/updateBalance
		mockWebServer.enqueue(new MockResponse().setResponseCode(updateBalanceReturnCode));

		// POST /transactions
		mockWebServer.enqueue(new MockResponse().setResponseCode(createTransactionReturnCode));
	}

	private static String buildCardJson(
			final long id, final long userId, final String code, final BigDecimal balance, final String cvc) {
		return CARD_JSON.formatted(
				id,
				userId,
				code,
				balance.toPlainString(),
				Instant.now().toString(),
				"ACTIVE",
				1,
				cvc,
				YearMonth.now().plusYears(1).toString());
	}

	@Test
	void testBalanceOperation_shouldSucceed() throws InterruptedException {
		final BalanceOperationRequest request = new BalanceOperationRequest("123", "456", new BigDecimal("50"), TransactionType.TRANSFER);

		balanceOperation_helperFunction(new BigDecimal("100"), 200, 200);

		final Mono<Void> result = service.balanceOperation(request);

		StepVerifier.create(result)
			.verifyComplete();

		// Inspect all requests received by MockWebServer
		for (int i = 1; i <= 5; i++) {
			final RecordedRequest req = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
			log.debug("Request {}: {}", i, (req != null ? req.getMethod() + " " + req.getPath() : "NONE"));
		}
	}

	@Test
	void testBalanceOperation_shouldFail() {

		final BalanceOperationRequest request = new BalanceOperationRequest("123", "456", new BigDecimal("50"), TransactionType.TRANSFER);
		balanceOperation_helperFunction(new BigDecimal("100"), 500, 200);

		final Mono<Void> result = service.balanceOperation(request);

		StepVerifier.create(result)
			.expectError()
			.verify();
	}

	/**
	 * Tests the behavior of the spend operation under rate-limiting conditions.
	 * Verifies that a card is allowed to perform up to 5 spends within a one-minute window,
	 * but the sixth spend attempt results in a {@link RateLimitExceededException}.
	 *
	 * The test sets up mock responses for the necessary interactions:
	 * - Fetching the card's balance.
	 * - Updating the card's balance.
	 * - Creating a transaction.
	 *
	 * The test first performs 5 successful spends, ensuring the balance is updated correctly after each.
	 * It then verifies that the 6th spend fails with the expected rate-limiting exception.
	 *
	 * Assertions verify the following:
	 * - Successful spends result in the remaining balance being correctly updated.
	 * - The appropriate exception is thrown when the rate limit is exceeded, with the expected error message.
	 */
	@Test
	void testSpend_rateLimitExceeded() {

		final BalanceOperationRequest request = new BalanceOperationRequest("123", "456", new BigDecimal("10"), TransactionType.TRANSFER);

		// Enqueue 6 sets of responses for GET /cards, PUT balance, POST transaction
		for (int i = 0; i < 6; i++) {
			// GET card balance (assume 100)
			// PUT update balance (200 OK)
			// POST create transaction (200 OK)
			balanceOperation_helperFunction(new BigDecimal("100"), 200, 200);
		}

		// The first 5 spends should succeed, 6th should fail with RateLimitExceededException
		StepVerifier.create(service.balanceOperation(request)).verifyComplete();
		StepVerifier.create(service.balanceOperation(request)).verifyComplete();
		StepVerifier.create(service.balanceOperation(request)).verifyComplete();
		StepVerifier.create(service.balanceOperation(request)).verifyComplete();
		StepVerifier.create(service.balanceOperation(request)).verifyComplete();

		StepVerifier.create(service.balanceOperation(request))
			.expectErrorMatches(throwable -> throwable instanceof RateLimitExceededException
					&& throwable.getMessage().contains("Max 5 spends per minute exceeded"))
			.verify();
	}

}
