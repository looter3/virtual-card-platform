package com.virtualcard.cardservice.controller;

import static com.virtualcard.common.lang.EndpointConstants.CARDS;
import static com.virtualcard.common.lang.EndpointConstants.CARD_NUMBER;
import static com.virtualcard.common.lang.EndpointConstants.GET_ALL_CARDS_BY_USER_MAPPING;
import static com.virtualcard.common.lang.EndpointConstants.GET_COVERED_CARD_MAPPING;
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

import com.virtualcard.cardservice.service.CardService;
import com.virtualcard.cardservice.service.UserIntegrationService;
import com.virtualcard.common.dto.CardDTO;
import com.virtualcard.common.request.AddCardRequest;
import com.virtualcard.common.request.UpdateBalanceRequest;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Flux;
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
	private final UserIntegrationService userIntegrationService;

	/**
	 * Handles the creation of a new card by delegating the request to the card service.
	 *
	 * @param cardRequestMono a {@link Mono} emitting the {@link AddCardRequest} object containing the cardholder's name
	 *                            and the initial balance for the card to be created
	 * @return a {@link Mono} that completes when the card creation process is finished
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Void> createCard(@RequestBody final Mono<AddCardRequest> cardRequestMono) {
		return cardRequestMono
			.flatMap(req -> cardService.createCard(req.username(), req.cardNumber(), req.expiration(), req.cvc()));
	}

	/**
	 * Retrieves a valid card based on the provided identifier.
	 *
	 * @param cardNumber the unique identifier of the card to be retrieved
	 * @return a {@code Mono<CardDTO>} containing the details of the valid card
	 */
	@GetMapping(CARD_NUMBER)
	public Mono<CardDTO> getCard(@PathVariable final String cardNumber) {
		return cardService.getValidCard(cardNumber);
	}

	@GetMapping(GET_ALL_CARDS_BY_USER_MAPPING)
	public Flux<CardDTO> getAllCardsByUsername(@PathVariable final String username) {
		return cardService.getAllCardsByUsername(username);
	}

	/**
	 * Retrieves a covered card based on the given card ID and amount.
	 *
	 * @param cardNumber the unique identifier of the card
	 * @param amount     the amount that the card should cover
	 * @return a Mono emitting the CardDTO object representing the covered card
	 */
	@GetMapping(GET_COVERED_CARD_MAPPING)
	public Mono<CardDTO> getCoveredCard(@PathVariable final String cardNumber, @RequestParam final BigDecimal amount) {
		return cardService.getValidCoveredCard(cardNumber, amount);
	}

	/**
	 * Updates the balance of a card specified by its unique identifier.
	 *
	 * @param id      the unique identifier of the card whose balance needs to be updated
	 * @param request the request containing the new balance to be set for the card
	 * @return a Mono signaling when the update operation has completed
	 */
	@PutMapping(UPDATE_BALANCE_MAPPING)
	public Mono<Void> updateBalance(@PathVariable final Long id,
			@RequestBody final UpdateBalanceRequest request) {
		return cardService.updateBalance(id, request.newBalance());
	}

}
