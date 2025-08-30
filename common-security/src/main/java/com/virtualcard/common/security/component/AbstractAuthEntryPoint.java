package com.virtualcard.common.security.component;

import java.nio.charset.StandardCharsets;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * @author Lorenzo Leccese
 *
 *         5 lug 2025
 *
 */
public abstract class AbstractAuthEntryPoint implements ServerAuthenticationEntryPoint {

	@Override
	public Mono<Void> commence(final ServerWebExchange exchange, final AuthenticationException authException) {
		final ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.UNAUTHORIZED);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

		final byte[] bytes = ("{\"error\": \"" + authException.getMessage() + "\"}").getBytes(StandardCharsets.UTF_8);
		final DataBuffer buffer = response.bufferFactory().wrap(bytes);

		return response.writeWith(Mono.just(buffer));
	}
}
