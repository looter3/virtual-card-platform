package com.virtualcard.card_aggregate.service;

import static com.virtualcard.common.lang.EndpointConstants.AMOUNT_QUERY;
import static com.virtualcard.common.lang.EndpointConstants.CARDS;
import static com.virtualcard.common.lang.EndpointConstants.GET_COVERED_CARD_URL;
import static com.virtualcard.common.lang.EndpointConstants.TRANSACTIONS;
import static com.virtualcard.common.lang.EndpointConstants.UPDATE_BALANCE;
import static com.virtualcard.common.lang.LangConstants.SLASH;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.jooq.generated.enums.TransactionType;
import com.jooq.generated.tables.pojos.CardDTO;
import com.virtualcard.card_aggregate.configuration.IntegrationProperties;
import com.virtualcard.common.error.InvalidInputException;
import com.virtualcard.common.error.RateLimitExceededException;
import com.virtualcard.common.request.CreateTransactionRequest;
import com.virtualcard.common.request.UpdateBalanceRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.NotFoundException;
import reactor.core.publisher.Mono;

/**
 * @author Lorenzo Leccese
 *
 *         7 giu 2025
 *
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class CardAggregateIntegrationService {

	private final WebClient webClient;
	private final IntegrationProperties integrationProperties;
	private String cardServiceBaseUrl;
	private String transactionServiceBaseUrl;

	/**
	 * A thread-safe map that tracks the spend counts for each card on a per-minute basis.
	 * The keys represent card identifiers (e.g., cardId), and the values are {@link AtomicInteger} instances
	 * that record the number of spend operations attempted for the corresponding card.
	 *
	 * This variable is used to enforce a rate limit on spending operations. If a card exceeds
	 * the maximum allowed number of spend operations within a minute, further spending attempts
	 * will be rejected until the count resets.
	 *
	 * The counts are cleared periodically (every minute) using a scheduled task to ensure
	 * the rate limits are applied within the intended time window.
	 */
	private final ConcurrentHashMap<String, AtomicInteger> spendCounters = new ConcurrentHashMap<>();

	private static final int MAX_SPENDS_PER_MINUTE = 5;

	/**
	 * Initializes resources or configurations required by the `CardAggregateIntegrationService`.
	 *
	 * This method is executed after the dependency injection is complete, as indicated by the
	 * `@PostConstruct` annotation. It performs the following actions:
	 *
	 * 1. Fetches and assigns the base URL for the card service from `integrationProperties`.
	 * 2. Fetches and assigns the base URL for the transaction service from `integrationProperties`.
	 * 3. Sets up a scheduled task using a single-threaded executor to reset spend counters in
	 *    `spendCounters` map every minute.
	 */
	@PostConstruct
	private void init() {
		cardServiceBaseUrl = integrationProperties.getCardServiceBaseUrl();
		transactionServiceBaseUrl = integrationProperties.getTransactionServiceBaseUrl();

		// Scheduled task to reset spend counts every minute
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
				spendCounters::clear,
				1, 1, TimeUnit.MINUTES);
	}

	/**
	 * Performs a spend operation on the specified card by deducting the specified amount from its balance.
	 * The method enforces a rate limit of a maximum of 5 spends per minute for each card.
	 *
	 * @param cardId the unique identifier of the card on which the spend operation is to be performed
	 * @param amount the monetary amount to be spent from the card's balance
	 * @return a {@link Mono} emitting the updated balance of the card upon a successful operation,
	 *         or an error if the operation fails (e.g., rate limit exceeded or insufficient balance)
	 */
	public Mono<BigDecimal> spend(final String cardId, final BigDecimal amount) {
		final AtomicInteger counter = spendCounters.computeIfAbsent(cardId, k -> new AtomicInteger(0));
		if (counter.incrementAndGet() > MAX_SPENDS_PER_MINUTE) {
			return Mono.error(new RateLimitExceededException("Max 5 spends per minute exceeded for card " + cardId));
		}
		return balanceOperation(cardId, amount, TransactionType.SPEND)
			.doOnError(e -> counter.decrementAndGet()); // rollback count on failure
	}

	/**
	 * Adds the specified amount to the balance of the card identified by the given card ID.
	 *
	 * @param cardId the unique identifier of the card to which the balance will be credited
	 * @param amount the amount to be added to the card's balance
	 * @return a {@link Mono} emitting the new balance after the top-up operation is successfully completed
	 */
	public Mono<BigDecimal> topup(final String cardId, final BigDecimal amount) {
		return balanceOperation(cardId, amount, TransactionType.TOPUP);
	}

	/**
	 * Adjusts the balance of a card based on the specified transaction type (e.g., spend or top-up).
	 *
	 * @param cardId the unique identifier of the card
	 * @param amount the transaction amount to be spent or added
	 * @param type the type of transaction, either SPEND or TOPUP
	 * @return a {@code Mono<BigDecimal>} representing the updated balance of the card after the operation
	 * @throws IllegalArgumentException if the transaction type is unsupported
	 * @throws NotFoundException if the card does not exist, is blocked, or has insufficient balance
	 * @throws WebClientResponseException if an error occurs during the HTTP request
	 */
	private Mono<BigDecimal> balanceOperation(final String cardId, final BigDecimal amount, final TransactionType type) {
		final String getCardURL;
		final Function<BigDecimal, BigDecimal> balanceOperation;

		switch (type) {
			case SPEND -> {
				getCardURL = cardServiceBaseUrl + GET_COVERED_CARD_URL + cardId + AMOUNT_QUERY + amount;
				balanceOperation = balance -> balance.subtract(amount);
			}
			case TOPUP -> {
				getCardURL = cardServiceBaseUrl + CARDS + SLASH + cardId;
				balanceOperation = balance -> balance.add(amount);
			}
			default -> throw new IllegalArgumentException("Unexpected value: " + type);
		}

		return webClient.get().uri(getCardURL)
			.retrieve()
			.bodyToMono(CardDTO.class)
			.switchIfEmpty(Mono.error(new com.virtualcard.common.error.NotFoundException("Card number: " + cardId + " not found, blocked or insufficient balance")))
			.onErrorResume(WebClientResponseException.class, ex -> {
				handleException(ex);
				return Mono.empty();
			})
			.flatMap(cardDTO -> {
				final Mono<BigDecimal> atomicBalanceTransaction = atomicBalanceTransaction(cardId, amount, cardDTO, type, balanceOperation);
				return Mono
					.from(atomicBalanceTransaction);
			});
	}

	/**
	 * Performs an atomic balance transaction by updating the card balance and creating a transaction record.
	 * If both operations are successful, the new balance is returned.
	 *
	 * @param cardId the identifier of the card for which the transaction is being performed
	 * @param amount the amount involved in the transaction
	 * @param cardDTO the data transfer object containing card details, including the current balance
	 * @param type the type of the transaction (e.g., SPEND, TOPUP)
	 * @param balanceOperation a function that calculates the new balance based on the current balance
	 * @return a Mono emitting the new card balance after the transaction is successfully processed
	 */
	private Mono<BigDecimal> atomicBalanceTransaction(
			final String cardId,
			final BigDecimal amount,
			final CardDTO cardDTO,
			final TransactionType type,
			final Function<BigDecimal, BigDecimal> balanceOperation) {

		final BigDecimal newBalance = balanceOperation.apply(cardDTO.getBalance());
		final String updateBalanceURL = cardServiceBaseUrl + CARDS + SLASH + cardId + UPDATE_BALANCE;

		final UpdateBalanceRequest updateBalanceRequest = new UpdateBalanceRequest(newBalance);
		final CreateTransactionRequest createTransactionRequest = new CreateTransactionRequest(cardId, amount, type);

		final Mono<Void> updateBalance = webClient.put()
			.uri(updateBalanceURL)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(updateBalanceRequest)
			.retrieve()
			.bodyToMono(Void.class)
			.onErrorMap(WebClientResponseException.class, this::handleException);

		final Mono<Void> createTransaction = webClient.post()
			.uri(transactionServiceBaseUrl + TRANSACTIONS)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(createTransactionRequest)
			.retrieve()
			.bodyToMono(Void.class)
			.onErrorMap(WebClientResponseException.class, this::handleException);
		// only emit if both succeed
		return updateBalance
			.then(createTransaction)
			.thenReturn(newBalance);
	}

	/**
	 * Handles exceptions that occur during web client operations.
	 * Converts specific HTTP errors into domain-specific exceptions
	 * or logs unexpected errors and rethrows them.
	 *
	 * @param ex the exception to be handled, typically of type {@code Throwable}
	 *           or {@code WebClientResponseException}
	 * @return the processed {@code Throwable}, which might be a
	 *         domain-specific exception such as {@code NotFoundException} or
	 *         {@code InvalidInputException}, or the original exception if it
	 *         is unexpected
	 */
	private Throwable handleException(final Throwable ex) {
		if (ex instanceof final WebClientResponseException wcre) {
			return switch (HttpStatus.resolve(wcre.getStatusCode().value())) {
				case NOT_FOUND -> new NotFoundException(wcre.getResponseBodyAsString());
				case UNPROCESSABLE_ENTITY -> new InvalidInputException(wcre.getResponseBodyAsString());
				default -> {
					log.error("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
					log.error("Error body: {}", wcre.getResponseBodyAsString());
					yield ex;
				}
			};
		}
		log.warn("Got an unexpected error: {}, will rethrow it", ex.toString());
		return ex;
	}

}
