package com.virtualcard.transactionservice.controller;

import static com.virtualcard.common.lang.EndpointConstants.ID;
import static com.virtualcard.common.lang.EndpointConstants.TRANSACTIONS;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.jooq.generated.tables.pojos.TransactionDTO;
import com.virtualcard.common.request.CreateTransactionRequest;
import com.virtualcard.transactionservice.pagination.PagedTransactionResponse;
import com.virtualcard.transactionservice.service.TransactionService;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Mono;

/**
 * @author Lorenzo Leccese
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
	public Mono<PagedTransactionResponse> getTransactionsByCardId(
			@PathVariable("id") final String cardId,
			@RequestParam(defaultValue = "0") final int page,
			@RequestParam(defaultValue = "20") final int size) {
		return transactionService.getTransactionsByCardId(cardId, page, size);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<TransactionDTO> createTransaction(@RequestBody final Mono<CreateTransactionRequest> transactionRequestMono) {
		return transactionRequestMono
			.flatMap(req -> transactionService.createTransaction(req.cardId(), req.amount(), req.type()));
	}

}
