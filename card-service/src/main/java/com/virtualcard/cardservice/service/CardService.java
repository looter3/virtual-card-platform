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
 * @author lex_looter
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

	public Mono<Void> createCard(final String cardholderName, final BigDecimal initialBalance) {
		return repository.insertCardWithGeneratedUUID(cardholderName, initialBalance);
	}

	public Mono<CardDTO> getValidCoveredCard(final String id, final BigDecimal amount) {
		return this.getValidCard(id).filter(validator.canAfford(id, amount));
	}

	public Mono<CardDTO> getValidCard(final String id) {
		return repository.getCard(id).filter(validator.isCardValid(id));
	}

	public Mono<CardDTO> getCard(final String id) {
		return repository.getCard(id);
	}

	public Mono<Void> updateBalance(final String id, final BigDecimal newBalance) {
		return repository.updateBalanceByCardId(id, newBalance);
	}

}
