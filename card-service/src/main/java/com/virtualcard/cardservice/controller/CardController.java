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
 * @author Lorenzo Leccese
 *
 *         7 giu 2025
 *
 */
@RequiredArgsConstructor
@RestController
@RequestMapping(CARDS)
public class CardController {

	private final CardService cardService;

	/**
	 * Handles the creation of a new card by delegating the request to the card service.
	 *
	 * @param cardRequestMono a {@link Mono} emitting the {@link CardRequest} object containing the cardholder's name
	 *                        and the initial balance for the card to be created
	 * @return a {@link Mono} that completes when the card creation process is finished
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Void> createCard(@RequestBody final Mono<CardRequest> cardRequestMono) {
		return cardRequestMono
			.flatMap(req -> cardService.createCard(req.cardholderName(), req.initialBalance()));
	}

	/**
	 * Retrieves a valid card based on the provided identifier.
	 *
	 * @param id the unique identifier of the card to be retrieved
	 * @return a {@code Mono<CardDTO>} containing the details of the valid card
	 */
	@GetMapping(ID)
	public Mono<CardDTO> getCard(@PathVariable final String id) {
		return cardService.getValidCard(id);
	}

	/**
	 * Retrieves a covered card based on the given card ID and amount.
	 *
	 * @param id the unique identifier of the card
	 * @param amount the amount that the card should cover
	 * @return a Mono emitting the CardDTO object representing the covered card
	 */
	@GetMapping(GET_COVERED_CARD_MAPPING)
	public Mono<CardDTO> getCoveredCard(@PathVariable final String id, @RequestParam final BigDecimal amount) {
		return cardService.getValidCoveredCard(id, amount);
	}

	/**
	 * Updates the balance of a card specified by its unique identifier.
	 *
	 * @param id the unique identifier of the card whose balance needs to be updated
	 * @param request the request containing the new balance to be set for the card
	 * @return a Mono signaling when the update operation has completed
	 */
	@PutMapping(UPDATE_BALANCE_MAPPING)
	public Mono<Void> updateBalance(@PathVariable final String id, @RequestBody final UpdateBalanceRequest request) {
		return cardService.updateBalance(id, request.newBalance());
	}

}
