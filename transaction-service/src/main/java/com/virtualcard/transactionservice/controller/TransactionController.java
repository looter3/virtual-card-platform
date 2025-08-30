package com.virtualcard.transactionservice.controller;

import static com.virtualcard.common.lang.EndpointConstants.GET_TRANSACTIONS_LAST_MONTH_BY_CARD_ID;
import static com.virtualcard.common.lang.EndpointConstants.ID;
import static com.virtualcard.common.lang.EndpointConstants.TRANSACTIONS;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.virtualcard.common.dto.TransactionDTO;
import com.virtualcard.common.request.CreateTransactionRequest;
import com.virtualcard.transactionservice.pagination.PagedTransactionResponse;
import com.virtualcard.transactionservice.service.TransactionService;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Flux;
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
			@PathVariable("id") final Long cardId,
			@RequestParam(defaultValue = "0") final int page,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime upperBoundDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime lowerBoundDate,
			@RequestParam(defaultValue = "20") final int size) {

		// Use MIN if lowerBoundDate is null
		final OffsetDateTime lower = lowerBoundDate != null ? lowerBoundDate : OffsetDateTime.of(LocalDateTime.of(1991, 1, 1, 1, 1), ZoneOffset.UTC);

		return transactionService.getTransactionsByCardId(cardId, page, upperBoundDate, lower, size);
	}

	@GetMapping(GET_TRANSACTIONS_LAST_MONTH_BY_CARD_ID)
	public Flux<TransactionDTO> getCurrentMonthTransactionsByCardId(
			@PathVariable("id") final Long cardId) {
		return transactionService.getAllCurrentMonthTransactionsByCardId(cardId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<TransactionDTO> createTransaction(@RequestBody final Mono<CreateTransactionRequest> transactionRequestMono) {
		return transactionRequestMono
			.flatMap(req -> transactionService.createTransaction(req.senderCardId(), req.recipientCardId(), req.amount(), req.type()));
	}

}
