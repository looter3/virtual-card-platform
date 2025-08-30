package com.virtualcard.card_aggregate.controller;

import static com.virtualcard.common.lang.EndpointConstants.BALANCE_OPERATION;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.virtualcard.card_aggregate.service.CardAggregateIntegrationService;
import com.virtualcard.common.request.BalanceOperationRequest;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Mono;

/**
 * @author Lorenzo Leccese
 *
 *         7 giu 2025
 *
 */
@RequiredArgsConstructor
@RestController
//@RequestMapping(CARDS_AGGREGATE)
public class CardAggregateController {

	private final CardAggregateIntegrationService integrationService;

	@PostMapping(BALANCE_OPERATION)
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Void> balanceOperation(@RequestBody final BalanceOperationRequest req) {
		return integrationService.balanceOperation(req);
	}

}
