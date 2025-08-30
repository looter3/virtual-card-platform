package com.virtualcard.edgeserver.auth.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.virtualcard.common.dto.Credentials;
import com.virtualcard.common.security.service.AbstractUserDetailsService;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Mono;

/**
 * @author Lorenzo Leccese
 *
 *         5 lug 2025
 *
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl extends AbstractUserDetailsService {

	private final UserIntegrationService integrationService;

	@Override
	protected Mono<Credentials> provideValidUserByUsername(final String username) throws UsernameNotFoundException {
		return integrationService.getCredentialsUsername(username)
			.map(usr -> new Credentials(usr.username(), usr.password(), usr.role()));
	}

}
