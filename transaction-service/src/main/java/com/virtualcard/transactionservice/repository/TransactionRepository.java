package com.virtualcard.transactionservice.repository;

import static com.jooq.generated.tables.Transaction.TRANSACTION;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.jooq.generated.enums.TransactionType;
import com.jooq.generated.tables.pojos.TransactionDTO;
import com.virtualcard.common.persistence.repository.AbstractJooqRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Lorenzo Leccese
 *
 *         8 giu 2025
 *
 */
@Repository
public class TransactionRepository extends AbstractJooqRepository {

	public TransactionRepository(final DSLContext dsl) {
		super(dsl);
	}

	public Mono<TransactionDTO> getTransaction(final String id) {
		return Mono.fromCallable(() -> dsl.selectFrom(TRANSACTION)
			.where(TRANSACTION.ID.eq(id))
			.fetchOneInto(TransactionDTO.class));
	}

	public Mono<TransactionDTO> insertTransactionFromDTO(final TransactionDTO dto) {
		return insertTransaction(dto.getCardid(), dto.getAmount(), dto.getType());
	}

	public Mono<TransactionDTO> insertTransaction(final String cardId, final BigDecimal amount, final TransactionType type) {
		final String id = UUID.randomUUID().toString();
		return Mono.fromCallable(() -> dsl.insertInto(TRANSACTION)
			.set(TRANSACTION.ID, id)
			.set(TRANSACTION.CARDID, cardId)
			.set(TRANSACTION.TYPE, type)
			.set(TRANSACTION.AMOUNT, amount)
			.set(TRANSACTION.CREATEDAT, LocalDateTime.now())
			.returning()
			.fetchOneInto(TransactionDTO.class));
	}

	public Flux<TransactionDTO> getTransactionsByCardId(final String cardId, final int offset, final int limit) {
		return Flux.fromIterable(
				dsl.selectFrom(TRANSACTION)
					.where(TRANSACTION.CARDID.eq(cardId))
					.orderBy(TRANSACTION.CREATEDAT.desc())
					.limit(limit)
					.offset(offset)
					.fetchInto(TransactionDTO.class));
	}

	public Mono<Integer> getTransactionCountByCardId(final String cardId) {
		return Mono.fromCallable(() -> dsl.selectCount()
			.from(TRANSACTION)
			.where(TRANSACTION.CARDID.eq(cardId))
			.fetchOne(0, int.class));
	}

}
