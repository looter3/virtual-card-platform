package com.virtualcard.transactionservice.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.jooq.generated.enums.TransactionType;
import com.jooq.generated.tables.pojos.TransactionDTO;
import com.virtualcard.transactionservice.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author lex_looter
 *
 *         7 giu 2025
 *
 */
@RequiredArgsConstructor
@Service
public class TransactionService {

	private final TransactionRepository repository;

	public Flux<TransactionDTO> getTransactions(final String cardId) {
		return repository.getTransactionsByCardId(cardId);
	}

	public Mono<TransactionDTO> createTransaction(final String cardId, final BigDecimal amount, final TransactionType type) {
		return repository.insertTransaction(cardId, amount, type);
	}

}
