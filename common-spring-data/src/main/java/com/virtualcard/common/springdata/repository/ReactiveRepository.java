package com.virtualcard.common.springdata.repository;

import org.hibernate.reactive.mutiny.Mutiny;
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory;

import com.virtualcard.common.springdata.entity.BaseEntity;

import io.smallrye.mutiny.Uni;

/**
 * @author Lorenzo Leccese
 *
 *         3 ago 2025
 *
 */
public abstract class ReactiveRepository<E extends BaseEntity> {

	protected final Mutiny.SessionFactory sessionFactory;

	private final Class<E> entityClass;

	public ReactiveRepository(final SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
		this.entityClass = provideEntityClass();
	}

	public Uni<E> findById(final Long id) {
		return sessionFactory.withSession(session -> session.find(this.entityClass, id));
	}

	public Uni<E> save(final E entity) {
		return sessionFactory.withSession(session -> session.persist(entity)
			.call(session::flush)
			.replaceWith(entity) // return the saved entity
		);
	}

	protected abstract Class<E> provideEntityClass();

}
