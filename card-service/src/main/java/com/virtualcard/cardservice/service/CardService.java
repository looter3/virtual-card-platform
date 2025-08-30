package com.virtualcard.cardservice.service;

import static com.virtualcard.common.converter.VertxWebFluxConverter.convertMultiToFlux;
import static com.virtualcard.common.converter.VertxWebFluxConverter.convertUniToMono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;

import org.springframework.stereotype.Service;

import com.virtualcard.cardservice.entity.Card;
import com.virtualcard.cardservice.mapper.CardMapper;
import com.virtualcard.cardservice.repository.ReactiveCardRepository;
import com.virtualcard.cardservice.validator.CardValidator;
import com.virtualcard.common.dto.CardDTO;
import com.virtualcard.common.enums.CardStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import reactor.core.publisher.Flux;
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

	private final UserIntegrationService userIntegrationService;

	private final ReactiveCardRepository repository;
	private final CardValidator validator;

	private final CardMapper mapper;

	/**
	 * Creates a new card with a generated unique identifier for the given cardholder name and initial balance.
	 * The card is persisted in the repository.
	 *
	 * @param cardholderName the name of the cardholder for whom the card is being created
	 * @param initialBalance the initial monetary balance to be associated with the newly created card
	 * @return a {@link Mono} that completes when the card is successfully created
	 */
	public Mono<Void> createCard(final String username, final String cardNumber, final YearMonth expiration, final String cvc) {

		return userIntegrationService.findUserByUsername(username)
			.doOnSubscribe(s -> log.debug("Fetching user for {}", username))
			.flatMap(usr -> {
				log.debug("User fetched: {}", usr);

				final Card card = new Card();
				card.setUserId(usr.getId());
				card.setCode(cardNumber);
				card.setStatus(activateCard() ? CardStatus.ACTIVE : CardStatus.BLOCKED);
				card.setBalance(BigDecimal.ZERO);
				card.setCreatedAt(Instant.now());
				card.setExpirationDate(expiration);
				card.setCvc(cvc);

				return convertUniToMono(repository.save(card))
					.doOnSubscribe(sub -> log.debug("Subscribing to save card"))
					.doOnNext(savedCard -> log.debug("Card saved: {}", savedCard))
					.doOnError(err -> log.error("Error saving card: {}", err));
			})
			.then()
			.doOnSuccess(v -> log.debug("createCard completed"))
			.doOnError(err -> log.error("createCard failed: {}", err));
	}

	// Dummy method that encapsulate Card activation controls
	private boolean activateCard() {
		return true;
	}

	/**
	 * Retrieves a valid card and checks if it has sufficient balance to cover the specified amount.
	 *
	 * @param cardNumber the unique identifier of the card
	 * @param amount     the amount to validate against the card balance
	 * @return a Mono emitting the valid and sufficiently covered CardDTO, or an empty Mono if the card is invalid or cannot cover the amount
	 */
	public Mono<CardDTO> getValidCoveredCard(final String cardNumber, final BigDecimal amount) {
		return this.getValidCard(cardNumber)
			.filter(validator.canAfford(amount));
	}

	/**
	 * Retrieves a card by its card number and validates it to ensure it is not blocked.
	 *
	 * @param cardNumber the unique identifier of the card to retrieve
	 * @return a {@code Mono} emitting the {@code CardDTO} if the card exists and is valid,
	 *         or an empty {@code Mono} if the card is invalid or does not exist
	 */
	public Mono<CardDTO> getValidCard(final String cardNumber) {
		return Mono.fromCompletionStage(repository.findByCode(cardNumber)
			.map(mapper::entityToDTO)
			.subscribeAsCompletionStage())
			.filter(validator.isCardValid());
	}

	/**
	 * Retrieves the card information for the given card ID.
	 *
	 * @param id the unique identifier of the card to be retrieved
	 * @return a {@code Mono<CardDTO>} containing the card information if found, or an empty Mono if no card is found
	 */
	public Mono<CardDTO> getCard(final Long id) {
		return Mono.fromCompletionStage(repository.findById(id).map(mapper::entityToDTO)
			.subscribeAsCompletionStage());
	}

	public Flux<CardDTO> getAllCardsByUsername(final String username) {

		return userIntegrationService.findUserByUsername(username)
			.flatMapMany(user -> convertMultiToFlux(repository.findByUserId(user.getId())))
			.map(mapper::entityToDTO);
	}

	/**
	 * Updates the balance of a card with the specified ID to a new value.
	 *
	 * @param id         the unique identifier of the card whose balance needs to be updated
	 * @param newBalance the new balance to be set for the card
	 * @return a Mono signaling when the update operation has completed
	 */
	public Mono<Void> updateBalance(final Long id, final BigDecimal newBalance) {
		return Mono.fromCompletionStage(repository.updateCardBalance(id, newBalance).subscribeAsCompletionStage()).then();
	}

}
