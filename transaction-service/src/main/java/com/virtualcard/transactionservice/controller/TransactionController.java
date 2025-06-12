package com.virtualcard.transactionservice.controller;

import static com.virtualcard.common.lang.EndpointConstants.ID;
import static com.virtualcard.common.lang.EndpointConstants.TRANSACTIONS;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.jooq.generated.tables.pojos.TransactionDTO;
import com.virtualcard.common.request.CreateTransactionRequest;
import com.virtualcard.transactionservice.service.TransactionService;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author lex_looter
 *
 *         8 giu 2025
 *
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(TRANSACTIONS)
public class TransactionController {

	private final TransactionService transactionService;

	@GetMapping(ID)
	public Flux<TransactionDTO> getTransactions(@PathVariable final String cardId) {
		return transactionService.getTransactions(cardId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<TransactionDTO> createTransaction(@RequestBody final Mono<CreateTransactionRequest> transactionRequestMono) {
		return transactionRequestMono
			.flatMap(req -> transactionService.createTransaction(req.cardId(), req.amount(), req.type()));
	}

}
