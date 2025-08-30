package com.virtualcard.edgeserver.auth.service;

import static com.virtualcard.common.lang.EndpointConstants.CREDENTIALS;
import static com.virtualcard.common.lang.LangConstants.SLASH;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.virtualcard.common.dto.Credentials;
import com.virtualcard.user_integration.service.AbstractUserIntegrationService;

import reactor.core.publisher.Mono;

/**
 * @author Lorenzo Leccese
 *
 *         5 lug 2025
 *
 */
@Service
public class UserIntegrationService extends AbstractUserIntegrationService {

	public UserIntegrationService(final WebClient webClient) {
		super(webClient);
	}

	public Mono<Credentials> getCredentialsUsername(final String username) {

		final StringBuilder sb = new StringBuilder();
		sb.append(userServiceBaseUrl);
		sb.append(CREDENTIALS);
		sb.append(SLASH);
		sb.append(username);

		final String url = sb.toString();

		return webClient.get()
			.uri(url)
			.retrieve()
			.bodyToMono(Credentials.class)
			.switchIfEmpty(Mono.error(new RuntimeException("User: " + username + " not found")));
	}

}
