package com.virtualcard.common.persistence.repository;

/**
 * @author Lorenzo Leccese
 *
 *         8 giu 2025
 *
 */
/*-
@Repository
public class JooqTransactionRepository extends AbstractJooqRepository {

	public JooqTransactionRepository(final DSLContext dsl) {
		super(dsl);
	}

	public Mono<TransactionDTO> findTransactionById(final String id) {
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

	public Flux<TransactionDTO> getLastMonthTransactionsByCardId(final String cardId) {
		return Flux.fromIterable(
				dsl.selectFrom(TRANSACTION)
					.where(TRANSACTION.CARDID.eq(cardId)
						.and(TRANSACTION.CREATEDAT.lessThan(DateUtils.getLastDayOfTheMonth()))
						.and(TRANSACTION.CREATEDAT.greaterThan(DateUtils.getFirstDayOfTheMonth())))
					.orderBy(TRANSACTION.CREATEDAT.desc())
					.fetchInto(TransactionDTO.class));
	}

	public Flux<TransactionDTO> getTransactionsByCardIdKeyset(final String cardId, final OffsetDateTime upperBoundDate, final OffsetDateTime lowerBoundDate, final int limit) {
		return Flux.fromIterable(
				dsl.selectFrom(TRANSACTION)
					.where(TRANSACTION.CARDID.eq(cardId)
						.and(TRANSACTION.CREATEDAT.lessOrEqual(upperBoundDate.toLocalDateTime()))
						.and(TRANSACTION.CREATEDAT.greaterOrEqual(lowerBoundDate.toLocalDateTime())))
					.orderBy(TRANSACTION.CREATEDAT.desc())
					.limit(limit)
					.fetchInto(TransactionDTO.class));
	}

	public Mono<Integer> getTransactionCountByCardId(final String cardId) {
		return Mono.fromCallable(() -> dsl.selectCount()
			.from(TRANSACTION)
			.where(TRANSACTION.CARDID.eq(cardId))
			.fetchOne(0, int.class));
	}

}
*/
