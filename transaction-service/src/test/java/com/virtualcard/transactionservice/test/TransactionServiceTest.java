package com.virtualcard.transactionservice.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jooq.generated.enums.TransactionType;
import com.jooq.generated.tables.pojos.TransactionDTO;
import com.virtualcard.transactionservice.repository.TransactionRepository;
import com.virtualcard.transactionservice.service.TransactionService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

	@InjectMocks
	private TransactionService service;

	@Mock
	private TransactionRepository repository;

	@Test
	void getTransactionsByCardId_shouldReturnPagedResults() {
		// Given
		final String cardId = "test-card-id";
		final int page = 0;
		final int size = 2;
		final int totalElements = 5;

		final List<TransactionDTO> transactions = List.of(
				createTransaction("1", cardId, BigDecimal.TEN),
				createTransaction("2", cardId, BigDecimal.TEN));

		// Mock repository behavior
		Mockito.when(repository.getTransactionsByCardId(cardId, page * size, size))
			.thenReturn(Flux.fromIterable(transactions));
		Mockito.when(repository.getTransactionCountByCardId(cardId))
			.thenReturn(Mono.just(totalElements));

		// When & Then
		StepVerifier.create(service.getTransactionsByCardId(cardId, page, size))
			.consumeNextWith(response -> {
				assertThat(response.getTransactions()).hasSize(2);
				assertThat(response.getMetadata().getCurrentPage()).isEqualTo(0);
				assertThat(response.getMetadata().getPageSize()).isEqualTo(2);
				assertThat(response.getMetadata().getTotalElements()).isEqualTo(5);
				assertThat(response.getMetadata().getTotalPages()).isEqualTo(3);
				assertThat(response.getMetadata().isHasNext()).isTrue();
				assertThat(response.getMetadata().isHasPrevious()).isFalse();
			})
			.verifyComplete();
	}

	@Test
	void getTransactionsByCardId_shouldReturnEmptyPage() {
		// Given
		final String cardId = "non-existing-card";
		final int page = 0;
		final int size = 20;

		// Mock repository behavior
		Mockito.when(repository.getTransactionsByCardId(cardId, page * size, size))
			.thenReturn(Flux.empty());
		Mockito.when(repository.getTransactionCountByCardId(cardId))
			.thenReturn(Mono.just(0));

		// When & Then
		StepVerifier.create(service.getTransactionsByCardId(cardId, page, size))
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
		final String cardId = "test-card-id";
		final int page = 2; // Last page
		final int size = 2;
		final int totalElements = 5;

		final List<TransactionDTO> transactions = List.of(
				createTransaction("5", cardId, BigDecimal.TEN));

		// Mock repository behavior
		Mockito.when(repository.getTransactionsByCardId(cardId, page * size, size))
			.thenReturn(Flux.fromIterable(transactions));
		Mockito.when(repository.getTransactionCountByCardId(cardId))
			.thenReturn(Mono.just(totalElements));

		// When & Then
		StepVerifier.create(service.getTransactionsByCardId(cardId, page, size))
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
		final String cardId = "test-card-id";
		final int page = -1; // Invalid page
		final int size = 20;

		// When & Then
		assertThrows(IllegalArgumentException.class,
				() -> service.getTransactionsByCardId(cardId, page, size));
	}

	@Test
	void getTransactionsByCardId_shouldHandleNegativeSize() {
		// Given
		final String cardId = "test-card-id";
		final int page = 0;
		final int size = -1; // Invalid size

		// When & Then
		assertThrows(IllegalArgumentException.class,
				() -> service.getTransactionsByCardId(cardId, page, size));
	}

	private TransactionDTO createTransaction(final String id, final String cardId, final BigDecimal amount) {
		return new TransactionDTO(
				id,
				cardId,
				TransactionType.SPEND,
				amount,
				LocalDateTime.now());
	}
}
