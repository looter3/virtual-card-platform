package com.virtualcard.common.security.service;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;

import com.virtualcard.common.dto.Credentials;

import reactor.core.publisher.Mono;

/**
 * @author Lorenzo Leccese
 *
 *         5 lug 2025
 *
 */
public abstract class AbstractUserDetailsService implements ReactiveUserDetailsService {

	@Override
	public Mono<UserDetails> findByUsername(final String username) {
		return provideValidUserByUsername(username)
			.map(currentUser -> org.springframework.security.core.userdetails.User
				.withUsername(currentUser.username())
				.password(currentUser.password())
				.roles(currentUser.role()) // assumes single role; use .roles(...) for multiple
				.build());
	}

	protected abstract Mono<Credentials> provideValidUserByUsername(final String username);

}
