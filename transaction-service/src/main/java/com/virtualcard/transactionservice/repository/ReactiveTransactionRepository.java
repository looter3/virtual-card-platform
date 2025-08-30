package com.virtualcard.transactionservice.repository;

import java.time.Instant;

import org.hibernate.reactive.mutiny.Mutiny.SessionFactory;
import org.springframework.stereotype.Repository;

import com.virtualcard.common.springdata.repository.ReactiveRepository;
import com.virtualcard.transactionservice.entity.Transaction;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

/**
 * @author Lorenzo Leccese
 *
 *         9 ago 2025
 *
 */
@Repository
public class ReactiveTransactionRepository
		extends ReactiveRepository<Transaction> {

	public ReactiveTransactionRepository(final SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	private final String FIND_BY_CARDID_WITHIN_INTERVAL = """
					SELECT t FROM Transaction t
					WHERE (t.senderCardId = :cardId OR t.recipientCardId = :cardId)
					  AND t.createdAt >= :lowerBound
					  AND t.createdAt <= :higherBound
					ORDER BY t.createdAt DESC
			""";

	public Multi<Transaction> findAllTransactionsWithinIntervalByCardId(final Long cardId, final Instant lowerBound, final Instant higherBound) {
		return sessionFactory.withSession(session -> {
			final var query = session.createQuery(FIND_BY_CARDID_WITHIN_INTERVAL, Transaction.class);
			query.setParameter("cardId", cardId);
			query.setParameter("lowerBound", lowerBound);
			query.setParameter("higherBound", higherBound);
			// getResultList returns Uni<List<Transaction>>
			return query.getResultList();
		}).onItem().transformToMulti(list -> Multi.createFrom().iterable(list));
	}

	public Multi<Transaction> findAllTransactionsWithinIntervalByCardId(final Long cardId, final Instant lowerBound, final Instant higherBound, final int page, final int size) {
		return sessionFactory.withSession(session -> {
			final var query = session.createQuery(FIND_BY_CARDID_WITHIN_INTERVAL, Transaction.class);
			query.setParameter("cardId", cardId);
			query.setParameter("lowerBound", lowerBound);
			query.setParameter("higherBound", higherBound);
			query.setFirstResult(page * size);
			query.setMaxResults(size);
			return query.getResultList();
		}).onItem().transformToMulti(list -> Multi.createFrom().iterable(list));
	}

	public Uni<Long> countTransactionsByCardId(final Long cardId) {
		return sessionFactory.withSession(session -> {
			final String countQuery = "SELECT COUNT(t) FROM Transaction t WHERE t.senderCardId = :cardId OR t.recipientCardId = :cardId";
			final var query = session.createQuery(countQuery, Long.class);
			query.setParameter("cardId", cardId);
			return query.getSingleResult();
		});
	}

	@Override
	protected Class<Transaction> provideEntityClass() {
		return Transaction.class;
	}

}
