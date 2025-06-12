package com.virtualcard.cardservice.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.jooq.generated.tables.pojos.CardDTO;
import com.virtualcard.cardservice.repository.CardRepository;
import com.virtualcard.cardservice.validator.CardValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import reactor.core.publisher.Mono;

/**
 * @author Lorenzo Leccese
 *
 *         7 giu 2025
 *
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class CardService {


	private final CardRepository repository;
	private final CardValidator validator;

	/**
	 * Creates a new card with a generated unique identifier for the given cardholder name and initial balance.
	 * The card is persisted in the repository.
	 *
	 * @param cardholderName the name of the cardholder for whom the card is being created
	 * @param initialBalance the initial monetary balance to be associated with the newly created card
	 * @return a {@link Mono} that completes when the card is successfully created
	 */
	public Mono<Void> createCard(final String cardholderName, final BigDecimal initialBalance) {
		return repository.insertCardWithGeneratedUUID(cardholderName, initialBalance);
	}

	/**
	 * Retrieves a valid card and checks if it has sufficient balance to cover the specified amount.
	 *
	 * @param id the unique identifier of the card
	 * @param amount the amount to validate against the card balance
	 * @return a Mono emitting the valid and sufficiently covered CardDTO, or an empty Mono if the card is invalid or cannot cover the amount
	 */
	public Mono<CardDTO> getValidCoveredCard(final String id, final BigDecimal amount) {
		return this.getValidCard(id).filter(validator.canAfford(id, amount));
	}

	/**
	 * Retrieves a card by its ID and validates it to ensure it is not blocked.
	 *
	 * @param id the unique identifier of the card to retrieve
	 * @return a {@code Mono} emitting the {@code CardDTO} if the card exists and is valid,
	 *         or an empty {@code Mono} if the card is invalid or does not exist
	 */
	public Mono<CardDTO> getValidCard(final String id) {
		return repository.getCard(id).filter(validator.isCardValid(id));
	}

	/**
	 * Retrieves the card information for the given card ID.
	 *
	 * @param id the unique identifier of the card to be retrieved
	 * @return a {@code Mono<CardDTO>} containing the card information if found, or an empty Mono if no card is found
	 */
	public Mono<CardDTO> getCard(final String id) {
		return repository.getCard(id);
	}

	/**
	 * Updates the balance of a card with the specified ID to a new value.
	 *
	 * @param id the unique identifier of the card whose balance needs to be updated
	 * @param newBalance the new balance to be set for the card
	 * @return a Mono signaling when the update operation has completed
	 */
	public Mono<Void> updateBalance(final String id, final BigDecimal newBalance) {
		return repository.updateBalanceByCardId(id, newBalance);
	}

}
