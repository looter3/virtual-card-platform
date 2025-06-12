package com.virtualcard.card_aggregate.controller;

import static com.virtualcard.common.lang.EndpointConstants.CARDS_AGGREGATE;
import static com.virtualcard.common.lang.EndpointConstants.SPEND_MAPPING;
import static com.virtualcard.common.lang.EndpointConstants.TOPUP_MAPPING;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.virtualcard.card_aggregate.service.CardAggregateIntegrationService;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Mono;

/**
 * @author lex_looter
 *
 *         7 giu 2025
 *
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(CARDS_AGGREGATE)
public class CardAggregateController {

	private final CardAggregateIntegrationService integrationService;

	@PostMapping(SPEND_MAPPING)
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<BigDecimal> spend(@PathVariable("id") final String cardId, @RequestBody final BigDecimal amount) {
		return integrationService.spend(cardId, amount);
	}

	@PostMapping(TOPUP_MAPPING)
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<BigDecimal> topup(@PathVariable("id") final String cardId, @RequestBody final BigDecimal amount) {
		return integrationService.topup(cardId, amount);
	}

}
