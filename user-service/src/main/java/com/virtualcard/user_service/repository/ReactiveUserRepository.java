package com.virtualcard.user_service.repository;

import org.hibernate.reactive.mutiny.Mutiny.SessionFactory;
import org.springframework.stereotype.Repository;

import com.virtualcard.common.springdata.repository.ReactiveRepository;
import com.virtualcard.user_service.entity.User;

import io.smallrye.mutiny.Uni;

/**
 * @author Lorenzo Leccese
 *
 *         9 ago 2025
 *
 */
@Repository
public class ReactiveUserRepository extends ReactiveRepository<User> {

	public ReactiveUserRepository(final SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	public Uni<User> findByUsername(final String username) {
		return sessionFactory.withSession(session -> session
			.createQuery("from User where username = :username", User.class)
			.setParameter("username", username)
			.getSingleResultOrNull());
	}

	@Override
	protected Class<User> provideEntityClass() {
		return User.class;
	}

}
