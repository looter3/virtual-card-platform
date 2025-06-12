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
 * @author Lorenzo Leccese
 *
 *         7 giu 2025
 *
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(CARDS_AGGREGATE)
public class CardAggregateController {

	private final CardAggregateIntegrationService integrationService;

	/**
	 * Processes a spend request for a specific card and deducts the specified amount.
	 *
	 * @param cardId the unique identifier of the card to process the spend request.
	 * @param amount the amount to be deducted from the card's balance.
	 * @return a {@code Mono<BigDecimal>} representing the updated balance after the spend operation is completed.
	 */
	@PostMapping(SPEND_MAPPING)
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<BigDecimal> spend(@PathVariable("id") final String cardId, @RequestBody final BigDecimal amount) {
		return integrationService.spend(cardId, amount);
	}

	/**
	 * Handles the top-up operation for the card with the specified card ID and amount.
	 *
	 * @param cardId the ID of the card to be topped up
	 * @param amount the amount to top up on the card
	 * @return a Mono emitting the new balance of the card after the top-up has been successfully processed
	 */
	@PostMapping(TOPUP_MAPPING)
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<BigDecimal> topup(@PathVariable("id") final String cardId, @RequestBody final BigDecimal amount) {
		return integrationService.topup(cardId, amount);
	}

}
