package com.virtualcard.edgeserver.auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.RequestBody;

import com.virtualcard.common.security.service.AbstractJwtService;

import reactor.core.publisher.Mono;

/**
 * @author Lorenzo Leccese
 *
 *         5 lug 2025
 *
 */
public abstract class AbstractLoginController<JWTS extends AbstractJwtService> implements LoginControllerAPI {

	private final JWTS jwtService;
	private final ReactiveAuthenticationManager reactiveAuthenticationManager;

	public AbstractLoginController(final JWTS jwtService, final ReactiveAuthenticationManager authenticationManager) {
		this.jwtService = jwtService;
		this.reactiveAuthenticationManager = authenticationManager;
	}

	@Override
	public Mono<ResponseEntity<Void>> login(@RequestBody final AccountCredentials credentials) {
		final UsernamePasswordAuthenticationToken creds = new UsernamePasswordAuthenticationToken(credentials.username(), credentials.password());

		return reactiveAuthenticationManager.authenticate(creds)
			.map(auth -> {
				final String jwts = jwtService.getToken(auth.getName());
				return ResponseEntity.ok()
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwts)
					.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Authorization")
					.build();
			});
	}

}
