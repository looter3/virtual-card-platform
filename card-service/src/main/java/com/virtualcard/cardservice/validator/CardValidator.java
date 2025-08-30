package com.virtualcard.cardservice.validator;

import static com.virtualcard.common.enums.CardStatus.BLOCKED;

import java.math.BigDecimal;
import java.util.function.Predicate;

import org.springframework.stereotype.Component;

import com.virtualcard.common.dto.CardDTO;

import lombok.extern.log4j.Log4j2;

/**
 * @author Lorenzo Leccese
 *
 *         8 giu 2025
 *
 */
@Component
@Log4j2
public class CardValidator {

	/**
	 * Determines if a card is valid based on its status.
	 * A card is considered invalid if its status is BLOCKED.
	 *
	 * @param cardId the unique identifier of the card being validated
	 * @return a {@code Predicate} that evaluates whether a {@code CardDTO} instance is valid
	 */
	public Predicate<? super CardDTO> isCardValid() {
		return cardDTO -> {
			if (cardDTO.getStatus() == BLOCKED) {
				log.debug("Card number: {} is blocked", cardDTO.getCode());
				return false;
			}
			return true;
		};
	}

	/**
	 * Determines if a specified card can afford a given amount based on its balance.
	 *
	 * @param cardId the unique identifier of the card
	 * @param amount the amount to check if it can be afforded by the card
	 * @return a predicate that evaluates if the card has sufficient balance for the specified amount
	 */
	public Predicate<? super CardDTO> canAfford(final BigDecimal amount) {
		return cardDTO -> {
			final boolean result = cardDTO.getBalance().compareTo(amount) >= 0;
			log.debug("Checking card {}: balance={} amount={} -> {}", cardDTO.getCode(), cardDTO.getBalance(), amount, result);
			return result;
		};
	}

}
