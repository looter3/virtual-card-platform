package com.virtualcard.transactionservice.test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
import reactor.test.StepVerifier;

/**
 * @author lex_looter
 *
 *         10 giu 2025
 *
 */
//@SpringBootTest
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest /* extends AbstractMySQLTestContainerTest */ {

	@InjectMocks
	private TransactionService service;

	@Mock
	private TransactionRepository repository;

	@Test
	void getTransactionsByCardId_shouldReturnSomething() {
		final TransactionDTO dto = new TransactionDTO("id", "cardId", TransactionType.SPEND, BigDecimal.valueOf(100), LocalDateTime.now());

		// Mock repository behavior
		Mockito.when(repository.getTransactionsByCardId(dto.getCardid()))
			.thenReturn(Flux.just(dto));

		StepVerifier.create(service.getTransactions(dto.getCardid()))
			.expectNext(dto) // Expect the exact DTO
			.verifyComplete();
	}

	@Test
	void getTransactionsByCardId_shouldReturnNothing() {
		// Mock repository returning empty result
		Mockito.when(repository.getTransactionsByCardId("-1"))
			.thenReturn(Flux.empty());

		StepVerifier.create(service.getTransactions("-1"))
			.expectNextCount(0)
			.verifyComplete();
	}

}
