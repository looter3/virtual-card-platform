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

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.virtualcard.card_aggregate.configuration.IntegrationProperties;
import com.virtualcard.common.dto.CardDTO;
import com.virtualcard.common.enums.TransactionType;
import com.virtualcard.common.error.InvalidInputException;
import com.virtualcard.common.error.RateLimitExceededException;
import com.virtualcard.common.request.BalanceOperationRequest;
import com.virtualcard.common.request.CreateTransactionRequest;
import com.virtualcard.common.request.UpdateBalanceRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
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
	 * The keys represent card numbers (e.g., code), and the values are {@link AtomicInteger} instances
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
	 * `spendCounters` map every minute.
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

	public Mono<Void> balanceOperation(final BalanceOperationRequest req) {

		final String senderCardCode = req.senderCardNumber();
		final String recipientCardCode = req.recipientCardNumber();
		final BigDecimal amount = req.amount();

		// Rate limit check
		final AtomicInteger counter = spendCounters.computeIfAbsent(senderCardCode, k -> new AtomicInteger(0));
		if (counter.incrementAndGet() > MAX_SPENDS_PER_MINUTE) {
			return Mono.error(new RateLimitExceededException("Max 5 spends per minute exceeded for card " + senderCardCode));
		}

		final String getSenderCardURL = cardServiceBaseUrl + GET_COVERED_CARD_URL + senderCardCode + AMOUNT_QUERY + amount;
		final String getRecipientCardURL = cardServiceBaseUrl + CARDS + SLASH + recipientCardCode;

		// Retrieve/validate both cards
		final Mono<CardDTO> senderCardMono = webClient.get().uri(getSenderCardURL)
			.retrieve()
			.bodyToMono(CardDTO.class)
			.switchIfEmpty(Mono.error(new com.virtualcard.common.error.NotFoundException("Sender card number: " + senderCardCode + " not found, blocked or insufficient balance")))
			.onErrorResume(WebClientResponseException.class, ex -> {
				handleException(ex);
				return Mono.error(ex);
			});
		final Mono<CardDTO> recipientCardMono = webClient.get().uri(getRecipientCardURL)
			.retrieve()
			.bodyToMono(CardDTO.class)
			.switchIfEmpty(Mono.error(new com.virtualcard.common.error.NotFoundException("Recipient card number: " + recipientCardCode + " not found")))
			.onErrorResume(WebClientResponseException.class, ex -> {
				handleException(ex);
				return Mono.error(ex);
			});

		return Mono.zip(senderCardMono, recipientCardMono)
			.flatMap(tuple -> {
				final CardDTO senderCard = tuple.getT1();
				final CardDTO recipientCard = tuple.getT2();

				return atomicBalanceTransaction(senderCard, recipientCard, amount);
			})
			.doOnError(e -> counter.decrementAndGet()); // rollback counter on failure

	}

	/**
	 * Performs an atomic balance transaction by updating the card balance and creating a transaction record.
	 * If both operations are successful, the new balance is returned.
	 *
	 * @param cardId           the identifier of the card for which the transaction is being performed
	 * @param amount           the amount involved in the transaction
	 * @param senderCardDTO    the data transfer object containing card details, including the current balance
	 * @param type             the type of the transaction (e.g., SPEND, TOPUP)
	 * @param balanceOperation a function that calculates the new balance based on the current balance
	 * @return a Mono emitting the new card balance after the transaction is successfully processed
	 */
	@Transactional
	private Mono<Void> atomicBalanceTransaction(
			final CardDTO senderCardDTO,
			final CardDTO recipientCardDTO,
			final BigDecimal amount) {

		final BigDecimal newSenderBalance = senderCardDTO.getBalance().subtract(amount);
		final BigDecimal newRecipientBalance = recipientCardDTO.getBalance().add(amount);
		final Long senderCardId = senderCardDTO.getId();
		final Long recipientCardId = recipientCardDTO.getId();

		// Create the URLs
		final String updateSenderBalanceURL = cardServiceBaseUrl + CARDS + SLASH + senderCardId + UPDATE_BALANCE;
		final String updateRecipientBalanceURL = cardServiceBaseUrl + CARDS + SLASH + recipientCardId + UPDATE_BALANCE;

		final UpdateBalanceRequest updateSenderBalanceRequest = new UpdateBalanceRequest(newSenderBalance);
		final UpdateBalanceRequest updateRecipientBalanceRequest = new UpdateBalanceRequest(newRecipientBalance);
		// TODO transaction
		// type hardcoded
		// for now
		final CreateTransactionRequest createTransactionRequest = new CreateTransactionRequest(senderCardId, recipientCardId, amount, TransactionType.TRANSFER);

		// Update sender balance
		final Mono<Void> updateSenderBalance = webClient.put()
			.uri(updateSenderBalanceURL)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(updateSenderBalanceRequest)
			.retrieve()
			.bodyToMono(Void.class)
			.onErrorMap(WebClientResponseException.class, this::handleException);

		// Update recipient balance
		final Mono<Void> updateRecipientBalance = webClient.put()
			.uri(updateRecipientBalanceURL)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(updateRecipientBalanceRequest)
			.retrieve()
			.bodyToMono(Void.class)
			.onErrorMap(WebClientResponseException.class, this::handleException);

		// Register transaction
		final Mono<Void> createTransaction = webClient.post()
			.uri(transactionServiceBaseUrl + TRANSACTIONS)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(createTransactionRequest)
			.retrieve()
			.bodyToMono(Void.class)
			.onErrorMap(WebClientResponseException.class, this::handleException);

		// only emit if all succeed
		return updateSenderBalance
			.then(updateRecipientBalance)
			.then(createTransaction);
	}

	/**
	 * Handles exceptions that occur during web client operations.
	 * Converts specific HTTP errors into domain-specific exceptions
	 * or logs unexpected errors and rethrows them.
	 *
	 * @param ex the exception to be handled, typically of type {@code Throwable}
	 *               or {@code WebClientResponseException}
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
