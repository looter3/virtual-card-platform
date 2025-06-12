package com.virtualcard.cardservice.repository;

import static com.jooq.generated.tables.Card.CARD;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.jooq.generated.enums.CardStatus;
import com.jooq.generated.tables.pojos.CardDTO;
import com.jooq.generated.tables.records.CardRecord;
import com.virtualcard.common.persistence.repository.AbstractJooqRepository;

import lombok.extern.log4j.Log4j2;

import reactor.core.publisher.Mono;

/**
 * @author lex_looter
 *
 *         8 giu 2025
 *
 */
@Repository
@Log4j2
public class CardRepository extends AbstractJooqRepository {

//	private final DSLContext dsl;

	public CardRepository(final DSLContext dsl) {
		super(dsl);
	}

	public Mono<Void> insertCardFromDTO(final CardDTO dto) {
		return insertCard(dto.getId(), dto.getCardholdername(), dto.getBalance(), dto.getStatus());
	}

	public Mono<Void> insertCardWithGeneratedUUID(final String cardholderName, final BigDecimal initialBalance) {
		final String id = UUID.randomUUID().toString();
		return insertCard(id, cardholderName, initialBalance, CardStatus.ACTIVE);
	}

	// TODO Let the client create the UUID maybe using Luhn algorithm
	public Mono<Void> insertCardWithId(final String id, final String cardholderName, final BigDecimal initialBalance) {
		return insertCard(id, cardholderName, initialBalance, CardStatus.ACTIVE);
	}

	private Mono<Void> insertCard(final String id, final String cardholderName, final BigDecimal initialBalance, final CardStatus status) {
		return Mono.fromRunnable(() -> dsl.insertInto(CARD)
			.set(CARD.ID, id)
			.set(CARD.CARDHOLDERNAME, cardholderName)
			.set(CARD.BALANCE, initialBalance)
			.set(CARD.CREATEDAT, LocalDateTime.now())
			.set(CARD.STATUS, status)
			.set(CARD.VERSION, 0)
			.execute());
	}

	public Mono<CardDTO> getCard(final String id) {
		return Mono.fromCallable(() -> dsl.selectFrom(CARD)
			.where(CARD.ID.eq(id))
			.fetchOneInto(CardDTO.class));
	}

	public Mono<Void> updateBalanceByCardId(final String id, final BigDecimal newBalance) {
		return Mono.fromRunnable(() -> Optional.ofNullable(dsl.fetchOne(CARD, CARD.ID.eq(id)))
			.map(cardRecord -> cardRecord.setBalance(newBalance))
			.ifPresent(CardRecord::store));
	}

}
