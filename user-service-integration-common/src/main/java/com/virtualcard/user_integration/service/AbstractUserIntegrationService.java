package com.virtualcard.user_integration.service;

import static com.virtualcard.common.lang.LangConstants.SLASH;

import org.springframework.web.reactive.function.client.WebClient;

import com.virtualcard.common.dto.UserDTO;

import lombok.RequiredArgsConstructor;

import reactor.core.publisher.Mono;

/**
 * @author Lorenzo Leccese
 *
 *         5 lug 2025
 *
 */
@RequiredArgsConstructor
public abstract class AbstractUserIntegrationService {

	protected final WebClient webClient;
	protected static final String userServiceBaseUrl = "http://user-service/user";

	public Mono<UserDTO> findUserByUsername(final String username) {

		final StringBuilder sb = new StringBuilder();
		sb.append(userServiceBaseUrl);
		sb.append(SLASH);
		sb.append(username);

		final String url = sb.toString();

		return webClient.get()
			.uri(url)
			.retrieve()
			.bodyToMono(UserDTO.class)
			.switchIfEmpty(Mono.error(new RuntimeException("User: " + username + " not found")));
	}

}
