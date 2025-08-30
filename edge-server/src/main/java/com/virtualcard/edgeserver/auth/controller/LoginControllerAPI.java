package com.virtualcard.edgeserver.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import reactor.core.publisher.Mono;

/**
 * @author Lorenzo Leccese
 *
 *         5 lug 2025
 *
 */
public interface LoginControllerAPI {

	@PostMapping("/login")
	Mono<ResponseEntity<Void>> login(@RequestBody AccountCredentials credentials);

	public record AccountCredentials(String username, String password) {
	}
}
