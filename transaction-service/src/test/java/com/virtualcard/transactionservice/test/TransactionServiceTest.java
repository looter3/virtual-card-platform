package com.virtualcard.transactionservice.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.virtualcard.common.enums.TransactionType;
import com.virtualcard.transactionservice.entity.Transaction;
import com.virtualcard.transactionservice.mapper.TransactionMapper;
import com.virtualcard.transactionservice.repository.ReactiveTransactionRepository;
import com.virtualcard.transactionservice.service.TransactionService;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

	@InjectMocks
	private TransactionService service;

	@Mock
	private ReactiveTransactionRepository repository;

	@Mock
	private TransactionMapper mapper;

	@Test
	void getTransactionsByCardId_shouldReturnPagedResults() {
		// Given
		final Long cardId = 123L;
		final int page = 0;
		final int size = 2;
		final int totalElements = 5;

		final List<Transaction> transactions = List.of(
				createTransaction(1L, cardId, 3L, BigDecimal.TEN),
				createTransaction(2L, cardId, 4L, BigDecimal.TEN));

		// Mock repository behavior with argument matchers
		Mockito.when(repository.findAllTransactionsWithinIntervalByCardId(
				Mockito.eq(cardId),
				Mockito.any(Instant.class),
				Mockito.any(Instant.class),
				Mockito.eq(page),
				Mockito.eq(size)))
			.thenReturn(Multi.createFrom().iterable(transactions));

		Mockito.when(repository.countTransactionsByCardId(cardId))
			.thenReturn(Uni.createFrom().item((long) totalElements));

		// When & Then
		StepVerifier.create(service.getTransactionsByCardId(cardId, page, OffsetDateTime.MAX, OffsetDateTime.MIN, size))
			.consumeNextWith(response -> {
				assertThat(response.getTransactions()).hasSize(2);
				assertThat(response.getMetadata().getCurrentPage()).isEqualTo(0);
				assertThat(response.getMetadata().getPageSize()).isEqualTo(2);
				assertThat(response.getMetadata().getTotalElements()).isEqualTo(totalElements);
				assertThat(response.getMetadata().getTotalPages()).isEqualTo(3);
				assertThat(response.getMetadata().isHasNext()).isTrue();
				assertThat(response.getMetadata().isHasPrevious()).isFalse();
			})
			.verifyComplete();
	}

	@Test
	void getTransactionsByCardId_shouldReturnEmptyPage() {
		// Given
		final Long cardId = 123L;
		final int page = 0;
		final int size = 20;

		Mockito.when(repository.findAllTransactionsWithinIntervalByCardId(
				Mockito.eq(cardId),
				Mockito.any(Instant.class),
				Mockito.any(Instant.class),
				Mockito.eq(page),
				Mockito.eq(size)))
			.thenReturn(Multi.createFrom().empty());

		Mockito.when(repository.countTransactionsByCardId(cardId))
			.thenReturn(Uni.createFrom().item(0L));

		// When & Then
		StepVerifier.create(service.getTransactionsByCardId(cardId, page, OffsetDateTime.MAX, OffsetDateTime.MIN, size))
			.consumeNextWith(response -> {
				assertThat(response.getTransactions()).isEmpty();
				assertThat(response.getMetadata().getTotalElements()).isZero();
				assertThat(response.getMetadata().getTotalPages()).isZero();
				assertThat(response.getMetadata().isHasNext()).isFalse();
				assertThat(response.getMetadata().isHasPrevious()).isFalse();
			})
			.verifyComplete();
	}

	@Test
	void getTransactionsByCardId_shouldHandleLastPage() {
		// Given
		final Long cardId = 123L;
		final int page = 2; // Last page
		final int size = 2;
		final int totalElements = 5;

		final List<Transaction> transactions = List.of(
				createTransaction(5L, cardId, 6L, BigDecimal.TEN));

		Mockito.when(repository.findAllTransactionsWithinIntervalByCardId(
				Mockito.eq(cardId),
				Mockito.any(Instant.class),
				Mockito.any(Instant.class),
				Mockito.eq(page),
				Mockito.eq(size)))
			.thenReturn(Multi.createFrom().iterable(transactions));

		Mockito.when(repository.countTransactionsByCardId(cardId))
			.thenReturn(Uni.createFrom().item((long) totalElements));

		// When & Then
		StepVerifier.create(service.getTransactionsByCardId(cardId, page, OffsetDateTime.MAX, OffsetDateTime.MIN, size))
			.consumeNextWith(response -> {
				assertThat(response.getTransactions()).hasSize(1);
				assertThat(response.getMetadata().getCurrentPage()).isEqualTo(2);
				assertThat(response.getMetadata().isHasNext()).isFalse();
				assertThat(response.getMetadata().isHasPrevious()).isTrue();
			})
			.verifyComplete();
	}

	@Test
	void getTransactionsByCardId_shouldHandleNegativePage() {
		// Given
		final Long cardId = 123L;
		final int page = -1; // Invalid page
		final int size = 20;

		// When & Then
		assertThrows(IllegalArgumentException.class,
				() -> service.getTransactionsByCardId(cardId, page, OffsetDateTime.MAX, OffsetDateTime.MIN, size));
	}

	@Test
	void getTransactionsByCardId_shouldHandleNegativeSize() {
		// Given
		final Long cardId = 123L;
		final int page = 0;
		final int size = -1; // Invalid size

		// When & Then
		assertThrows(IllegalArgumentException.class,
				() -> service.getTransactionsByCardId(cardId, page, OffsetDateTime.MAX, OffsetDateTime.MIN, size));
	}

	private Transaction createTransaction(final Long id, final Long senderCardId, final Long recipientCardId, final BigDecimal amount) {
		return new Transaction(
				id,
				UUID.randomUUID().toString(),
				senderCardId,
				recipientCardId,
				TransactionType.TRANSFER,
				amount,
				Instant.now());
	}
}
