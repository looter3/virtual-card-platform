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
 * @author lex_looter
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

	// TODO In production we might want to use tools like Redis, caching a token binded with the cardId
	// Rate limiter state, key is cardId
	private final ConcurrentHashMap<String, AtomicInteger> spendCounters = new ConcurrentHashMap<>();
	private static final int MAX_SPENDS_PER_MINUTE = 5;

	@PostConstruct
	private void init() {
		cardServiceBaseUrl = integrationProperties.getCardServiceBaseUrl();
		transactionServiceBaseUrl = integrationProperties.getTransactionServiceBaseUrl();

		// Scheduled task to reset spend counts every minute
		Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
				spendCounters::clear,
				1, 1, TimeUnit.MINUTES);
	}

	public Mono<BigDecimal> spend(final String cardId, final BigDecimal amount) {
		final AtomicInteger counter = spendCounters.computeIfAbsent(cardId, k -> new AtomicInteger(0));
		if (counter.incrementAndGet() > MAX_SPENDS_PER_MINUTE) {
			return Mono.error(new RateLimitExceededException("Max 5 spends per minute exceeded for card " + cardId));
		}
		return balanceOperation(cardId, amount, TransactionType.SPEND)
			.doOnError(e -> counter.decrementAndGet()); // rollback count on failure
	}

	public Mono<BigDecimal> topup(final String cardId, final BigDecimal amount) {
		return balanceOperation(cardId, amount, TransactionType.TOPUP);
	}

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

		return updateBalance
			.then(createTransaction)
			.thenReturn(newBalance); // only emit if both succeed
	}

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
