package com.virtualcard.transactionservice.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.jooq.generated.enums.TransactionType;
import com.jooq.generated.tables.pojos.TransactionDTO;
import com.virtualcard.transactionservice.pagination.PagedTransactionResponse;
import com.virtualcard.transactionservice.pagination.PaginationMetadata;
import com.virtualcard.transactionservice.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Mono;

/**
 * @author Lorenzo Leccese
 *
 *         7 giu 2025
 *
 */
@RequiredArgsConstructor
@Service
public class TransactionService {

	private final TransactionRepository repository;

	public Mono<PagedTransactionResponse> getTransactionsByCardId(final String cardId, final int page, final int size) {
		// Validate input parameters
		if (page < 0) {
			throw new IllegalArgumentException("Page number cannot be negative");
		}
		if (size < 1) {
			throw new IllegalArgumentException("Page size must be greater than 0");
		}

		final int offset = page * size;

		return Mono.zip(
				repository.getTransactionsByCardId(cardId, offset, size).collectList(),
				repository.getTransactionCountByCardId(cardId))
			.map(tuple -> {
				final List<TransactionDTO> transactions = tuple.getT1();
				final int totalElements = tuple.getT2();

				return PagedTransactionResponse.builder()
					.transactions(transactions)
					.metadata(PaginationMetadata.builder()
						.currentPage(page)
						.pageSize(size)
						.totalElements(totalElements)
						.totalPages((int) Math.ceil((double) totalElements / size))
						.hasNext((page + 1) * size < totalElements)
						.hasPrevious(page > 0)
						.build())
					.build();
			});
	}

	public Mono<TransactionDTO> createTransaction(final String cardId, final BigDecimal amount, final TransactionType type) {
		return repository.insertTransaction(cardId, amount, type);
	}

}
