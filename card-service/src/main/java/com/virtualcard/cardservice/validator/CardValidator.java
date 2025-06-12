package com.virtualcard.cardservice.validator;

import java.math.BigDecimal;
import java.util.function.Predicate;

import org.springframework.stereotype.Component;

import com.jooq.generated.enums.CardStatus;
import com.jooq.generated.tables.pojos.CardDTO;
import com.virtualcard.common.lang.BigDecimalUtils;

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
	public Predicate<? super CardDTO> isCardValid(final String cardId) {
		return cardDTO -> {
			if (cardDTO.getStatus() == CardStatus.BLOCKED) {
				log.debug("Card with id: {} is blocked", cardId);
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
	public Predicate<? super CardDTO> canAfford(final String cardId, final BigDecimal amount) {
		return cardDTO -> {
			// If it's a spend operation check available balance
			if (BigDecimalUtils.lessThan(cardDTO.getBalance(), amount)) {
				log.debug("Insufficient balance for cardId: {}, balance: {}, charge: {}", cardId, cardDTO.getBalance(), amount);
				return false;
			}
			return true;
		};
	}

}
