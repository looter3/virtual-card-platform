package com.virtualcard.transactionservice.service;

import static com.virtualcard.common.converter.VertxWebFluxConverter.convertMultiToFlux;
import static com.virtualcard.common.converter.VertxWebFluxConverter.convertUniToMono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.virtualcard.common.dto.TransactionDTO;
import com.virtualcard.common.enums.TransactionType;
import com.virtualcard.common.lang.DateUtils;
import com.virtualcard.transactionservice.entity.Transaction;
import com.virtualcard.transactionservice.mapper.TransactionMapper;
import com.virtualcard.transactionservice.pagination.PagedTransactionResponse;
import com.virtualcard.transactionservice.pagination.PaginationMetadata;
import com.virtualcard.transactionservice.repository.ReactiveTransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import io.smallrye.mutiny.Uni;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Lorenzo Leccese
 *
 *         7 giu 2025
 *
 */
@Log4j2
@RequiredArgsConstructor
@Service
public class TransactionService {

//	private final JooqTransactionRepository repository;
	private final ReactiveTransactionRepository repository;
	private final TransactionMapper mapper;

	public Flux<TransactionDTO> getAllCurrentMonthTransactionsByCardId(final Long cardId) {
		final Instant lastDayOfTheMonth = DateUtils.getLastDayOfTheMonth();
		final Instant firstDayOfTheMonth = DateUtils.getFirstDayOfTheMonth();
		return convertMultiToFlux(repository.findAllTransactionsWithinIntervalByCardId(cardId, firstDayOfTheMonth, lastDayOfTheMonth)
			.map(mapper::entityToDTO));
	}

	public Mono<PagedTransactionResponse> getTransactionsByCardId(
			final Long cardId,
			final int page,
			final OffsetDateTime upperBoundDate,
			final OffsetDateTime lowerBoundDate,
			final int size) {

		if (page < 0) {
			throw new IllegalArgumentException("Page number cannot be negative");
		}
		if (size < 1) {
			throw new IllegalArgumentException("Page size must be greater than 0");
		}

		final Uni<List<TransactionDTO>> transactionListUni = repository
			.findAllTransactionsWithinIntervalByCardId(cardId, lowerBoundDate.toInstant(), upperBoundDate.toInstant(), page, size)
			.collect().asList()
			.onItem().transform(list -> list.stream()
				.map(mapper::entityToDTO)
				.toList());

		final Uni<Long> countUni = repository.countTransactionsByCardId(cardId);

		// Convert Uni to Mono
		final Mono<List<TransactionDTO>> transactionListMono = convertUniToMono(transactionListUni);
		final Mono<Long> countMono = convertUniToMono(countUni);

		// Zip Mono and build response
		return Mono.zip(transactionListMono, countMono)
			.map(tuple -> {
				final List<TransactionDTO> transactions = tuple.getT1();
				final long totalElements = tuple.getT2();

				final int totalPages = (int) Math.ceil((double) totalElements / size);

				return PagedTransactionResponse.builder()
					.transactions(transactions)
					.metadata(PaginationMetadata.builder()
						.currentPage(page)
						.pageSize(size)
						.totalElements((int) totalElements)
						.totalPages(totalPages)
						.hasNext((page + 1) * size < totalElements)
						.hasPrevious(page > 0)
						.build())
					.build();
			});
	}

	public Mono<TransactionDTO> createTransaction(final Long senderCardId, final Long recipientCardId, final BigDecimal amount, final TransactionType type) {

		log.debug("createTransaction called with senderCardId={}, recipientCardId={}, amount={}, type={}", senderCardId, recipientCardId, amount, type);

		final Transaction transaction = new Transaction();
		transaction.setSenderCardId(senderCardId);
		transaction.setRecipientCardId(recipientCardId);
		transaction.setCode(UUID.randomUUID().toString());
		transaction.setCreatedAt(Instant.now());
		transaction.setAmount(amount);
		transaction.setType(type);

		return convertUniToMono(repository.save(transaction))
			.doOnNext(saved -> log.debug("Saved transaction: {}", saved))
			.map(mapper::entityToDTO);
	}

}
