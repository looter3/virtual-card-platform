package com.virtualcard.cardservice.validator;

import java.math.BigDecimal;
import java.util.function.Predicate;

import org.springframework.stereotype.Component;

import com.jooq.generated.enums.CardStatus;
import com.jooq.generated.tables.pojos.CardDTO;
import com.virtualcard.common.lang.BigDecimalUtils;

import lombok.extern.log4j.Log4j2;

/**
 * @author lex_looter
 *
 *         8 giu 2025
 *
 */
@Component
@Log4j2
public class CardValidator {

	/*
	 * Checks that the given card isn't blocked
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

	/*
	 * Checks that the given card can spend the specified amount of money
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
