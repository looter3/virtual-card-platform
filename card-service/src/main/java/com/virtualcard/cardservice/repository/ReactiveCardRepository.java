package com.virtualcard.cardservice.repository;

import java.math.BigDecimal;

import org.hibernate.reactive.mutiny.Mutiny.SessionFactory;
import org.springframework.stereotype.Repository;

import com.virtualcard.cardservice.entity.Card;
import com.virtualcard.common.springdata.repository.ReactiveRepository;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.LockModeType;

/**
 * @author Lorenzo Leccese
 *
 *         9 ago 2025
 *
 */
@Repository
public class ReactiveCardRepository extends ReactiveRepository<Card> {

	public ReactiveCardRepository(final SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public Multi<Card> findByUserId(final Long userId) {
		return Multi.createFrom().deferred(() -> sessionFactory
			.withSession(session -> session.createQuery("from Card where userId = :userId", Card.class)
				.setParameter("userId", userId)
				.getResultList())
			.onItem().transformToMulti(list -> Multi.createFrom().iterable(list)));
	}

	public Uni<Card> findByCode(final String code) {
		return sessionFactory.withSession(session -> session
			.createQuery("from Card where code = :code", Card.class)
			.setParameter("code", code)
			.getSingleResultOrNull());
	}

	public Uni<Card> updateCardBalance(final Long id, final BigDecimal newBalance) {
		return sessionFactory.withTransaction((session, tx) -> session.find(Card.class, id, LockModeType.OPTIMISTIC)
			.onItem().ifNotNull().invoke(card -> card.setBalance(newBalance)));
	}

	@Override
	protected Class<Card> provideEntityClass() {
		return Card.class;
	}

}
