package com.virtualcard.cardservice.controller;

import static com.virtualcard.common.lang.EndpointConstants.CARDS;
import static com.virtualcard.common.lang.EndpointConstants.GET_COVERED_CARD_MAPPING;
import static com.virtualcard.common.lang.EndpointConstants.ID;
import static com.virtualcard.common.lang.EndpointConstants.UPDATE_BALANCE_MAPPING;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.jooq.generated.tables.pojos.CardDTO;
import com.virtualcard.cardservice.service.CardService;
import com.virtualcard.common.request.CardRequest;
import com.virtualcard.common.request.UpdateBalanceRequest;

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
@RequestMapping(CARDS)
public class CardController {

	private final CardService cardService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Void> createCard(@RequestBody final Mono<CardRequest> cardRequestMono) {
		return cardRequestMono
			.flatMap(req -> cardService.createCard(req.cardholderName(), req.initialBalance()));
	}

	@GetMapping(ID)
	public Mono<CardDTO> getCard(@PathVariable final String id) {
		return cardService.getValidCard(id);
	}

	@GetMapping(GET_COVERED_CARD_MAPPING)
	public Mono<CardDTO> getCoveredCard(@PathVariable final String id, @RequestParam final BigDecimal amount) {
		return cardService.getValidCoveredCard(id, amount);
	}

	@PutMapping(UPDATE_BALANCE_MAPPING)
	public Mono<Void> updateBalance(@PathVariable final String id, @RequestBody final UpdateBalanceRequest request) {
		return cardService.updateBalance(id, request.newBalance());
	}

}
