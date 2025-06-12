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
 * @author Lorenzo Leccese
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
	}

	/**
	 * Cleans up resources and performs necessary teardown operations after each test execution.
	 * Shuts down the mock web server to release associated resources and avoid potential conflicts
	 * with subsequent tests.
	 *
	 * @throws Exception if an error occurs during the shutdown process of the mock web server.
	 */
	@AfterEach
	void tearDown() throws Exception {
		mockWebServer.shutdown();
	}

	/**
	 * Simulates a balance operation process on a card involving multiple HTTP calls such as fetching the card details,
	 * updating the balance, and creating a transaction. It invokes the appropriate service method to handle
	 * the specified transaction type (e.g., SPEND or TOPUP). The return codes for the simulated HTTP responses
	 * are configurable for testing purposes.
	 *
	 * @param cardId the unique identifier of the card to perform the operation on
	 * @param balanceAsString the current balance of the card represented as a string
	 * @param amount the monetary amount to be used in the transaction
	 * @param type the type of transaction to be performed, such as SPEND or TOPUP
	 * @param updateBalanceReturnCode the HTTP response code that will be returned for the balance update operation
	 * @param createTransactionReturnCode the HTTP response code that will be returned for the transaction creation operation
	 * @return a {@link Mono} emitting the updated card balance as a {@link BigDecimal} upon successful operation,
	 *         or an error if the operation fails
	 */
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

	/**
	 * Verifies the successful top-up operation for a card. The test ensures that when a top-up
	 * transaction is performed, the resulting balance reflects the added amount correctly.
	 *
	 * This method uses a helper function to simulate the top-up operation, passing relevant parameters
	 * such as the card ID, current balance, amount to be added, transaction type, and expected HTTP
	 * response codes for balance update and transaction creation endpoints.
	 *
	 * Assertions are performed using reactive StepVerifier to check that:
	 * - The resulting balance matches the expected value after the top-up is processed.
	 * - The operation completes successfully without errors.
	 *
	 * Preconditions:
	 * - A card with the given ID and an initial balance exists.
	 * - The HTTP responses from the mocked services (balance update, transaction creation) are correctly
	 *   queued with the expected return codes.
	 */
	@Test
	void testTopup_shouldSucceed() {

		final Mono<BigDecimal> result = balanceOperation_helperFunction("card123", "100", new BigDecimal("50"), TransactionType.TOPUP, 200, 200);

		StepVerifier.create(result)
			.expectNext(new BigDecimal("150"))
			.verifyComplete();
	}

	/**
	 * Tests the failure scenario of the top-up operation on a card.
	 *
	 * The method uses a helper function to simulate the balance operation for a
	 * top-up transaction. It ensures that the operation fails by verifying that
	 * an error occurs during the process.
	 *
	 * Assertions:
	 * - Expects an error to be emitted when the top-up operation is executed.
	 *
	 * Components used in the test:
	 * - `balanceOperation_helperFunction`: Helper method to perform the transaction logic and
	 *   simulate interactions with external systems via mock responses.
	 * - `StepVerifier`: Used to verify the behavior of the Mono returned by the operation.
	 *
	 * Mock Responses:
	 * - Mocks HTTP responses for card balance retrieval, balance update, and transaction creation.
	 *
	 * Transaction Type:
	 * - Operates with `TransactionType.TOPUP`.
	 */
	@Test
	void testTopup_shouldFail() {

		final Mono<BigDecimal> result = balanceOperation_helperFunction("card123", "100", new BigDecimal("50"), TransactionType.TOPUP, 500, 200);

		StepVerifier.create(result)
			.expectError()
			.verify();
	}

	/**
	 * Tests the successful spending of a specified amount from a card's balance.
	 *
	 * This test case utilizes the `balanceOperation_helperFunction` to simulate the
	 * `SPEND` operation on a card using mocked responses for balance retrieval,
	 * balance update, and transaction creation. It verifies that the remaining
	 * balance is correctly calculated and returned.
	 *
	 * The test ensures:
	 * - The initial balance is correctly retrieved from the mocked server.
	 * - The specified spend amount is deducted from the initial balance.
	 * - The correct remaining balance is received as the result.
	 * - The operation completes successfully without errors.
	 */
	@Test
	void testSpend_shouldSucceed() {

		final Mono<BigDecimal> result = balanceOperation_helperFunction("card456", "200", new BigDecimal("75"), TransactionType.SPEND, 200, 200);

		StepVerifier.create(result)
			.expectNext(new BigDecimal("125"))
			.verifyComplete();
	}

	/**
	 * Test case to validate that the spend operation fails under specific conditions.
	 *
	 * The method invokes {@code balanceOperation_helperFunction} with predefined parameters,
	 * simulating a SPEND transaction scenario where an error is expected.
	 *
	 * Verifies:
	 * - The resulting {@link Mono} emits an error.
	 *
	 * Usage context:
	 * Typically used for testing abnormal behavior in SPEND operations, such as when
	 * insufficient balance or other error conditions prevent the transaction from succeeding.
	 *
	 * Assertions:
	 * - Uses {@link StepVerifier} to ensure an error is emitted during the operation.
	 */
	@Test
	void testSpend_shouldFail() {

		final Mono<BigDecimal> result = balanceOperation_helperFunction("card456", "60", new BigDecimal("75"), TransactionType.SPEND, 500, 200);

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
